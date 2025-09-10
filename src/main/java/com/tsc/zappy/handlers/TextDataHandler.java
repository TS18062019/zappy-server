package com.tsc.zappy.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TextDataHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established. Id:{}, {}, {}, {}, {}", session.getId(), session.getUri().toString(), session.getPrincipal(), session.getLocalAddress().getAddress(), session.getRemoteAddress().getAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
         log.info("Text payload length {} from {}", message.getPayloadLength(), session.getAttributes().get("deviceId"));
    }

     @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session closed! Id: {}, {}", session.getId(), status.getReason());
    }
    
}
