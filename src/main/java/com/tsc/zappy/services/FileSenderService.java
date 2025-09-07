package com.tsc.zappy.services;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tsc.zappy.components.TransferMap;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@Service
public class FileSenderService {
    
    private TransferMap map;
    private RestTemplate restTemplate;

    /**
     * Send the file set metadata to the receiver
     * Do not wait for confirmation here
     */
    public void notify(String url, Set<String> fileSet) {
        map.add(url, fileSet);
        var response = restTemplate.postForObject(url, fileSet, HttpStatus.class);
        log.info(response.value());
    }

    public void send(String uuid) {

    }
}
