package com.tsc.zappy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeerInfo {
    
    private final String name;
    private long timestamp;
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
