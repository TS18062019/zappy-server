package com.tsc.zappy.components;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.tsc.zappy.dto.PeerInfo;

@Component
public class PeerMapProvider {
    
    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    public Map<String, PeerInfo> getPeerMap() {
        return peerMap;
    }
}
