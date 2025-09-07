package com.tsc.zappy.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MulticastProperties {
    
    @Value("${multicast.addr}")
    private String multicastAddr;

    @Value("${multicast.port}")
    private int multicasrPort;

    @Value("${multicast.announce_interval}")
    private long announceIntervalMs;

    @Value("${multicast.no_response_timeout}")
    private long noResponseTimeOut;
    
    public String getMulticastAddr() {
        return multicastAddr;
    }
    public int getMulticasrPort() {
        return multicasrPort;
    }
    public long getAnnounceIntervalMs() {
        return announceIntervalMs;
    }
    public long getNoResponseTimeOut() {
        return noResponseTimeOut;
    }
}
