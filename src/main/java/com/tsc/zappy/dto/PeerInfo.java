package com.tsc.zappy.dto;

public class PeerInfo {
    
    private String name, ip;
    private long timestamp;
    
    public String getName() {
        return name;
    }
    public String getIp() {
        return ip;
    }
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public PeerInfo(String name, String ip, long timestamp) {
        this.name = name;
        this.ip = ip;
        this.timestamp = timestamp;
    }
}
