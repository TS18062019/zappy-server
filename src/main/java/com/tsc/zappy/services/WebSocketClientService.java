package com.tsc.zappy.services;

import java.util.concurrent.CompletionException;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebSocketClientService {
    
    private final HardwareInfo info;
    private final HmacUtil util;
    /**
     * 
     * @param url specifies the url of the websocket server to connect to
     * @return the associated session if successful
     */
    public void createNewSession(WebSocketHandler webSocketHandler, String host, String endpoint) {
        String thisDeviceId = info.getDeviceId();
        StandardWebSocketClient client = new StandardWebSocketClient();
        try {
            client.execute(webSocketHandler, "ws://{0}:8080/ws/{1}?deviceId={2}&sign={3}", host, endpoint, thisDeviceId, util.sign(thisDeviceId))
            .join();
        } catch (CompletionException e) {
            log.error("Could not connect to {}/{}", host, endpoint);
        } catch (IllegalArgumentException e) {
            log.error("Bad arguments: host={}, endpoint={}", host, endpoint);
        }
    }
}
