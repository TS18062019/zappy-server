package com.tsc.zappy.dto;

import java.util.List;

import lombok.Data;

@Data
public class WebSocketTextMessageDTO {
    
    private String destinationDeviceId;
    private String destinationIp;
    private String command;
    private List<MessageDataDTO> msgData;
}
