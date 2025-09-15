package com.tsc.zappy.interfaces;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.tsc.zappy.constants.Constants;
import com.tsc.zappy.dto.WebSocketTextMessageDTO;
import com.tsc.zappy.dto.WebSocketTextMessageResponseDTO;

import lombok.Data;

@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = WebSocketTextMessageDTO.class, name = Constants.REQUEST),
    @JsonSubTypes.Type(value = WebSocketTextMessageResponseDTO.class, name = Constants.RESPONSE)
})

@Data
public class WebSocketTextMessage {
    
    // this flag controls whether the receiving server needs to inform the sender of the delivery status => use when
    // you need to know the status of message delivery
    private boolean requiresResponse;
    private String sourceDeviceId;
    private String sourceIp;
    private String destinationDeviceId;
    private String destinationIp;

}
