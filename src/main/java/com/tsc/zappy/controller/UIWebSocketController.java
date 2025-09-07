package com.tsc.zappy.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.tsc.zappy.dto.PeerInfo;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@AllArgsConstructor
public class UIWebSocketController {
    
    private SimpMessagingTemplate sTemplate;

    @MessageMapping("/files")
    public void processFileMetaData(Set<String> listOfFiles) {
        log.info(listOfFiles.size());
    }

    public void sendToUser(Map<String, PeerInfo> peerMap) {
        try {
            sTemplate.convertAndSend("/zappy/activePeers", peerMap);
        } catch (MessagingException e) {
            log.error(e);
        }
    }
}
