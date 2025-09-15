package com.tsc.zappy.handlers;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.WebSocketSessionProvider;
import com.tsc.zappy.constants.Constants;
import com.tsc.zappy.dto.WebSocketSessionDTO;
import com.tsc.zappy.dto.WebSocketTextMessageDTO;
import com.tsc.zappy.dto.WebSocketTextMessageResponseDTO;
import com.tsc.zappy.interfaces.WebSocketTextMessage;
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
        WebSocketSessionDTO sessionDTO = new WebSocketSessionDTO();
        sessionDTO.setSession(session);
        // make session fetchable with both - ip & deviceId
        sessionDTO.setDeviceId(connectedDeviceId);
        sessionDTO.setDeviceIp(connectedServerIp);
        if (!clientService.getIpQueue().isEmpty())
            sessionDTO.setDeviceIp(clientService.getIpQueue().poll());
        sessionProvider.addSession(session.getId(), sessionDTO);
    }

    /**
     * Route based on ip then forward to matching deviceId
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Text payload of length {} received from {}", message.getPayloadLength(),
                getParamFromSession(session, Constants.DEVICE_ID));
        var dto = objectMapper.readValue(message.getPayload(), WebSocketTextMessage.class);
        String destinationDeviceId = dto.getDestinationDeviceId();
        String destinationDeviceIp = dto.getDestinationIp();
        String sourceDeviceId = dto.getSourceDeviceId();
        String sourceIp = dto.getSourceIp();
        // discard packet if ttl above threshold
        dto.incrementTtl();
        if (dto.getTtl() > 30) {
            log.info("Packet from={} to={} discarded", sourceIp, destinationDeviceIp);
            return;
        }
        if (info.getServerIp().equals(destinationDeviceIp)) {
            log.info("Message reached the destination server {}", destinationDeviceIp);
            // process message meant for this device
            if (info.getDeviceId().equals(destinationDeviceId)) {
                log.info("Message reached the destination device id={}", destinationDeviceId);
                if (dto instanceof WebSocketTextMessageDTO wtm)
                    localCommandsService.processCommand(wtm, session);
                trySendResponse(session, sourceIp, sourceDeviceId, null, Constants.SUCCESS);
            } else {
                // forward message to device connected with this server
                var existingSession = sessionProvider.getWebSocketSessionWithDeviceId(destinationDeviceId);
                existingSession.ifPresentOrElse(ses -> {
                    log.info("Forwarding message to {} with an existing session on this server", destinationDeviceId);
                    trySendMessage(ses, getNewTextMessage(dto));
                }, () -> {
                    log.error("No device with id {} found on this server", destinationDeviceId);
                    trySendResponse(session, sourceIp, sourceDeviceId, null, Constants.FAILED);
                });
            }
        } else {
            // get existing connected server ips or create new connection & forward to them
            var existingSession = sessionProvider.getWebSocketSessionWithDeviceIp(destinationDeviceIp);
            existingSession.ifPresentOrElse(ses -> {
                log.info("Forwarding message to server {} with an existing session", destinationDeviceIp);
                trySendMessage(ses, getNewTextMessage(dto));
            }, () -> {
                log.info("Creating a new session with server {} and forwarding this message...", destinationDeviceIp);
                clientService.createNewSessionAndForward(this, dto.getDestinationIp(), "text", getNewTextMessage(dto))
                .thenAccept(resp -> {
                    if(resp.equals(Constants.FAILED))
                        trySendResponse(session, sourceIp, sourceDeviceId, null, Constants.FAILED);
                });
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session closed! Id: {}, {}", session.getId(), status.getReason());
        sessionProvider.deleteSession(session.getId());
    }

    private String getParamFromSession(WebSocketSession session, String param) {
        return (String) session.getAttributes().get(param);
    }

    private TextMessage getNewTextMessage(WebSocketTextMessage message) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("JSON parse exception", e);
            throw new IllegalArgumentException(e);
        }
    }

    private void trySendMessage(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("Couldn't send message", e);
        }
    }

    private void trySendResponse(WebSocketSession session, String destnIp, String destnDeviceId, Object payload,
            String statusCode) {
        // necessary to prevent loopback
        if (destnDeviceId == null || destnIp == null || destnDeviceId.isBlank() || destnIp.isBlank())
            return;
        // IMP: source must not be set here or reset to ""
        WebSocketTextMessageResponseDTO dto = new WebSocketTextMessageResponseDTO(statusCode, Constants.RESPONSE, payload);
        dto.setDestinationDeviceId(destnDeviceId);
        dto.setDestinationIp(destnIp);
        dto.setSourceDeviceId("");
        dto.setSourceIp("");
        try {
            session.sendMessage(getNewTextMessage(dto));
        } catch (IOException e) {
            log.error("Couldn't send response", e);
        }
    }

}