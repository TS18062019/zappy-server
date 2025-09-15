package com.tsc.zappy.dto;

import java.util.List;

import com.tsc.zappy.interfaces.WebSocketTextMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebSocketTextMessageDTO extends WebSocketTextMessage {
    
    private String command;
    private List<MessageDataDTO> msgData;
    private String type;
    
}
