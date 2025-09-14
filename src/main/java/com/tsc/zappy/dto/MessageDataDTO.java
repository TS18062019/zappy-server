package com.tsc.zappy.dto;

import lombok.Data;

@Data
public class MessageDataDTO {

    private String type;
    private String payload;
    private String size;
}
