package com.tsc.zappy.components;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionProvider {
    
    private Map<String, WebSocketSession> map = new ConcurrentHashMap<>();

    public Optional<WebSocketSession> getWebSocketSession(String deviceId) {
        return Optional.ofNullable(map.get(deviceId));
    }

    public void addSession(String deviceId, WebSocketSession session) {
        map.putIfAbsent(deviceId, session);
    }

    public void deleteSession(String deviceId) {
        map.remove(deviceId);
    }
}
