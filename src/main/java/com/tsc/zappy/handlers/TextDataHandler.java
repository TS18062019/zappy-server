package com.tsc.zappy.handlers;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.WebSocketSessionProvider;
import com.tsc.zappy.constants.Constants;
import com.tsc.zappy.dto.WebSocketTextMessageDTO;
import com.tsc.zappy.services.LocalCommandsService;
import com.tsc.zappy.services.WebSocketClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class TextDataHandler extends TextWebSocketHandler {

    private final HardwareInfo info;
    private final ObjectMapper objectMapper;
    private final WebSocketClientService clientService;
    private final WebSocketSessionProvider sessionProvider;
    private final LocalCommandsService localCommandsService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established. Id:{}, {}, {}, {}, {}", session.getId(), session.getUri(),
                session.getPrincipal(), session.getLocalAddress().getAddress(),
                session.getRemoteAddress().getAddress());

        var connectedDeviceId = getParamFromSession(session, Constants.DEVICE_ID);
        var connectedServerIp = getParamFromSession(session, Constants.DEVICE_IP);
        // make session fetchable with both - ip & deviceId
        if(connectedDeviceId != null && !connectedDeviceId.equals(info.getDeviceId()))
            sessionProvider.addSession(connectedDeviceId, session);
        if(connectedServerIp != null && !connectedServerIp.equals(info.getServerIp()))
            sessionProvider.addSession(connectedServerIp, session);
    }

    /**
     * Route based on ip then forward to matching deviceId
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Text payload of length {} received from {}", message.getPayloadLength(),
                getParamFromSession(session, Constants.DEVICE_ID));
        WebSocketTextMessageDTO dto = objectMapper.readValue(message.getPayload(), WebSocketTextMessageDTO.class);
        String destinationDeviceId = dto.getDestinationDeviceId();
        String destinationDeviceIp = dto.getDestinationIp();
        if (info.getServerIp().equals(destinationDeviceIp)) {
            log.info("Message reached the destination server {}", destinationDeviceIp);
            // process message meant for this device
            if (info.getDeviceId().equals(destinationDeviceId)) {
                log.info("Message reached the detination device id={}", destinationDeviceId);
                localCommandsService.processCommand(dto, session);
            } else {
                // forward message to device connected with this server
                var existingSession = sessionProvider.getWebSocketSessionWithId(destinationDeviceId);
                existingSession.ifPresentOrElse(ses -> {
                    log.info("Forwarding message to {} with an existing session on this server", destinationDeviceId);
                    trySendMessage(session, message);
                }, () -> 
                    log.error("No device with id {} found on this server", destinationDeviceId)
                );
            }
        } else {
            // get existing connected server ips or create new connection & forward to them
            var existingSession = sessionProvider.getWebSocketSessionWithIp(destinationDeviceIp);
            existingSession.ifPresentOrElse(ses -> {
                log.info("Forwarding message to server {} with an existing session", destinationDeviceIp);
                trySendMessage(session, message);
            }, () -> {
                log.info("Creating a new session with server {} and forwarding this message...", destinationDeviceIp);
                clientService.createNewSessionAndForward(this, dto.getDestinationIp(), "text", message);
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session closed! Id: {}, {}", session.getId(), status.getReason());
        sessionProvider.deleteSession(getParamFromSession(session, Constants.DEVICE_ID));
        sessionProvider.deleteSession(getParamFromSession(session, Constants.DEVICE_IP));
    }

    private String getParamFromSession(WebSocketSession session, String param) {
        return (String) session.getAttributes().get(param);
    }

    private void trySendMessage(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("Couldn't send message", e);
        }
    }

}