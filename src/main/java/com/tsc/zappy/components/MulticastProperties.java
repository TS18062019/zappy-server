package com.tsc.zappy.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class MulticastProperties {
    
    @Value("${multicast.addr}")
    private String multicastAddr;

    @Value("${multicast.port}")
    private int multicasrPort;

    @Value("${multicast.announce_interval}")
    private long announceIntervalMs;

    @Value("${multicast.no_response_timeout}")
    private long noResponseTimeOut;

    @Value("${multicast.peers_refresh_interval}")
    private long peersRefreshInterval;
    
}
