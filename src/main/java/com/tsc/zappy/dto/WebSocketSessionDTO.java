package com.tsc.zappy.dto;

import org.springframework.web.socket.WebSocketSession;

import lombok.Data;

@Data
public class WebSocketSessionDTO {
    
    private WebSocketSession session;
    private String deviceId;
    private String deviceIp;
}
