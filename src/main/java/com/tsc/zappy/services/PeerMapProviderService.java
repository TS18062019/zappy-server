package com.tsc.zappy.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.tsc.zappy.dto.PeerInfo;

@Service
public class PeerMapProviderService {
    
    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    public Map<String, PeerInfo> getPeerMap() {
        return peerMap;
    }
}
