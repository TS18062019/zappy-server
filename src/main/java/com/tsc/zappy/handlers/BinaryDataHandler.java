package com.tsc.zappy.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class BinaryDataHandler extends BinaryWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established. Id:{}, {}, {}, {}, {}", session.getId(), session.getUri().toString(), session.getPrincipal(), session.getLocalAddress().getAddress(), session.getRemoteAddress().getAddress());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        log.info("Binary payload length {}", message.getPayloadLength());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session closed! Id: {}, {}", session.getId(), status.getReason());
    }

}