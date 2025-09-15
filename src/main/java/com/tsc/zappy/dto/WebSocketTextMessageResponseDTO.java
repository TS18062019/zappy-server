package com.tsc.zappy.dto;

import com.tsc.zappy.interfaces.WebSocketTextMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketTextMessageResponseDTO extends WebSocketTextMessage {
    
    private String status;
    private String type;
    private Object payload;

}
