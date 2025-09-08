package com.tsc.zappy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatagramFormat {
    
    private String name;
    private String nonce;
    private String address;
    private String signature;
    
    public DatagramFormat(String nonce, String name, String address) {
        this.nonce = nonce;
        this.address = address;
        this.name = name;
    }

}
