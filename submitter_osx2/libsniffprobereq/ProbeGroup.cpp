//
//  ProbeGroup.cpp
//  shenanigans
//
//  Created by dr on 7/18/14.
//
//

#include "ProbeGroup.h"
#include <tins/tins.h>

ProbeGroup::ProbeGroup(std::string& _mac){
    mac = _mac;
}

ProbeGroup::ProbeGroup(ProbeReq& req){
    mac = req.mac;
    addProbe(req);
}


ProbeGroup::~ProbeGroup(){
    std::vector<ProbeReq*>::iterator ptr;

    for(ptr = probeReqs.begin(); ptr != probeReqs.end(); ptr++) {
        delete *ptr;
    }
}

bool ProbeGroup::probeSeen(ProbeReq& req){
    std::map<std::string, ProbeReq*>::iterator mapPtr = probeMap.find(req.ssid);
    if (req.ssid == SHENANIGANS){
        consent = true;
    }
    bool newProbe = false;
    if (mapPtr == probeMap.end()){
        addProbe(req);
        newProbe = true;
    } else {
        ProbeReq* req = mapPtr->second;
        req->lastSeen.update();
        newProbe = false;
    }
    return newProbe;
}

void ProbeGroup::addProbe(ProbeReq& req){
    ProbeReq* newReq = new ProbeReq(req);
    probeReqs.push_back(newReq);
    
    probeMap[req.ssid] = newReq;
    printf("New probe: %s %s\n", newReq->mac.c_str(), newReq->ssid.c_str());
}
/*
ProbeReq::ProbeReq(const ProbeReq& req){
    ssid = req.ssid;
    mac = req.mac;
    lastSeen = req.lastSeen
}*/