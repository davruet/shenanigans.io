#pragma once

#include <vector>
#include <string>
#include <map>
#include <tins/tins.h>
#include "ProbeGroup.h"
#include "Poco/Thread.h"
#include <functional>

#define STATUS_NEW_GROUP 0
#define STATUS_NEW_SSID 1
#define STATUS_REPEATED_PROBE 2

#define QUEUE_MAX 200


typedef std::function<void(ProbeGroup*, ProbeReq*, int)> ProbeGroupListener;
typedef std::map<std::string, ProbeGroup*> ProbeGroupMapType;

// FIXME - This whole library should be a submodule.
class ProbeReqSniffer {
 
    class SnifferRunnable: public Poco::Runnable {


    public:
        SnifferRunnable();
        ~SnifferRunnable();
        Poco::Mutex launchMutex;
        bool initialized = false;
        bool running = false;
        std::vector<ProbeReq>packetQueue;
        Poco::Mutex queueMutex;
        Tins::Sniffer *sniffer;
        std::string interface;
        // the thread function
        void run();
        
        bool handle(Tins::PDU& pdu);
        
        void drainQueue(std::vector<ProbeReq>* returnQueue);
        
    };

private:
    Poco::Thread thread;
    SnifferRunnable snifferRunnable;
    std::vector<ProbeReq> packetBuffer;
    // keep track of all probe groups seen.
    ProbeGroupMapType groupMap;
    bool noconsent = true;
    std::vector<ProbeGroupListener> groupListeners;
    std::string interface;

    
public:
    ProbeReqSniffer();
    ~ProbeReqSniffer();
    void update();
    void start(std::string interface);
    void stop();
    pcap_t *pcap;
    struct pcap_pkthdr header;
    const unsigned char* packet = NULL;
    std::list<std::string> packetHex;
    uint state;
    ProbeGroupMapType* getGroupMap();
    void addNewGroupListener(ProbeGroupListener newGroup);


};




