//
//  ProbeGroup.h
//  shenanigans
//
//  Created by dr on 7/18/14.
//
//
#pragma once
#include <string>
#include <vector>
#include <map>
#include <Poco/Timestamp.h>


#define SHENANIGANS "shenanigans"

typedef std::vector<uint8_t> packet_bytes;

class ProbeReq {
    
public:
    
    ProbeReq(std::string s, std::string m, packet_bytes raw){
        ssid = s;
        mac = m;
        lastSeen.update();
        rawBytes = raw;
    }
    
    ProbeReq(){};
    std::string ssid;
    std::string mac;
    std::string key;
    packet_bytes rawBytes;
    Poco::Timestamp lastSeen;
    
    bool operator<(const ProbeReq& s1) const{
        int cmp = mac.compare(s1.mac);
        if (cmp == 0){
            int ssidCmp = ssid.compare(s1.ssid);
            return ssidCmp < 0;
        } else {
            return cmp < 0;
        }
        
    }
    struct comparator {
        bool operator () (ProbeReq* req_1, ProbeReq* req_2) const {
            return (*req_1 < *req_2);
        }
    };

};


class ProbeGroup {
    
public:
    Poco::Timestamp firstSeen;
    Poco::Timestamp lastSeen;
    //unsigned int sightingCount = 0; FIXME - this might be useful.
    bool consent = false;
    bool inDatabase = false;
    std::string mac = "";
    std::vector<ProbeReq*> probeReqs;
    std::map<std::string, ProbeReq*> probeMap;
    
    ProbeGroup(){}
    ProbeGroup(std::string& mac);
    ProbeGroup(ProbeReq& req);
    ~ProbeGroup();
    /* Notify the group that a probe has been seen. If the probe is new, it will be added to the group's
     list of probe reqs. If not, the probe's lastSeen property will be updated with the current timestamp.
     Returns true if the probe was added, false if it was previously seen.
     */
    bool probeSeen(ProbeReq& req);
    
private:
    void addProbe(ProbeReq& req);
};