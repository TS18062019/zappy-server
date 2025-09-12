package com.tsc.zappy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatagramFormat {
    
    private String name;
    private String deviceId;
    private String address;
    private String signature;
    
    public DatagramFormat(String deviceId, String name, String address) {
        this.deviceId = deviceId;
        this.address = address;
        this.name = name;
    }

}
