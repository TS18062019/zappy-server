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
        sessionProvider.addSession((String) session.getAttributes().get(Constants.DEVICE_ID), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Text payload of length {} received from {}", message.getPayloadLength(),
                session.getAttributes().get(Constants.DEVICE_ID));
        WebSocketTextMessageDTO dto = objectMapper.readValue(message.getPayload(), WebSocketTextMessageDTO.class);
        String destinationDeviceId = dto.getDestinationDeviceId();
        // if message is meant for this device accept it else forward it
        if (destinationDeviceId.equals(info.getDeviceId())) {
            localCommandsService.processCommand(dto, session);
        } else {
            var existingSession = sessionProvider.getWebSocketSession(destinationDeviceId);
            existingSession.ifPresentOrElse(anotherSession -> {
                try {
                    anotherSession.sendMessage(message);
                } catch (IOException e) {
                    log.error("Exception occurred while sending message to {}. Reason: {}", destinationDeviceId,
                            e.getMessage());
                }
            }, () -> clientService.createNewSession(this, dto.getDestinationIp(), "text"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session closed! Id: {}, {}", session.getId(), status.getReason());
        sessionProvider.deleteSession((String) session.getAttributes().get(Constants.DEVICE_ID));
    }

}
