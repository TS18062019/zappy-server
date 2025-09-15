package com.tsc.zappy.services;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.constants.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebSocketClientService {

    private final HardwareInfo info;
    private final HmacUtil util;

    private Queue<String> outgoingDeviceIp = new ConcurrentLinkedQueue<>();

    /**
     * 
     * @param url specifies the url of the websocket server to connect to
     * @return the associated session if successful
     */
    public CompletableFuture<String> createNewSessionAndForward(WebSocketHandler webSocketHandler, String host,
            String endpoint,
            WebSocketMessage<?> message) {
        String thisDeviceId = info.getDeviceId();
        StandardWebSocketClient client = new StandardWebSocketClient();
        outgoingDeviceIp.add(host);
        return client
                .execute(webSocketHandler, "ws://{0}:8080/ws/{1}?deviceId={2}&sign={3}", host, endpoint, thisDeviceId,
                        util.sign(thisDeviceId))
                .thenApply(session -> {
                    String status = "";
                    try {
                        session.sendMessage(message);
                        status = Constants.SUCCESS;
                    } catch (IOException e) {
                        log.error("Couldn't forward message to {}/{}", host, endpoint);
                        status = Constants.FAILED;
                    }
                    return status;
                }).exceptionally(ex -> {
                    log.error("Oops! Some error occured while connecting to {}/{}", host, endpoint, ex);
                    outgoingDeviceIp.clear();
                    return Constants.FAILED;
                });
    }

    public Queue<String> getIpQueue() {
        return outgoingDeviceIp;
    }
}
