package com.tsc.zappy.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class MulticastProperties {
    
    @Value("${multicast.addr}")
    private final String multicastAddr;

    @Value("${multicast.port}")
    private final int multicasrPort;

    @Value("${multicast.announce_interval}")
    private final long announceIntervalMs;

    @Value("${multicast.no_response_timeout}")
    private final long noResponseTimeOut;
    
}
