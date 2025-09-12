package com.tsc.zappy.dto;

import lombok.Data;

@Data
public class WebSocketTextMessageDTO {
    
    private String destinationDeviceId;
    private String destinationIp;
    private String payload;
}
