package com.tsc.zappy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketTextMessageResponseDTO {
    
    private String type;
    private Object payload;
}
