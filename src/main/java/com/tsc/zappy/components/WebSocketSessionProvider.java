package com.tsc.zappy.components;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.tsc.zappy.interfaces.SessionChangeListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class WebSocketSessionProvider {

    private Map<String, List<SessionChangeListener>> listenersMap = new ConcurrentHashMap<>();
    private Map<String, WebSocketSession> map = new ConcurrentHashMap<>();

    /**
     * Registers session change listeners for a particular device
     * @param listener
     * @param deviceId
     */
    public void registerListener(SessionChangeListener listener, String deviceId) {
        log.info("Registering listener: {} for device {}", listener.getClass().getName(), deviceId);
        listenersMap.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void unregisterListener(SessionChangeListener listener, String deviceId) {
        log.info("De-registering listener: {} for device {}", listener.getClass().getName(), deviceId);
        listenersMap.getOrDefault(deviceId, Collections.emptyList()).remove(listener);
    }

    public Optional<WebSocketSession> getWebSocketSession(String deviceId) {
        return Optional.ofNullable(map.get(deviceId));
    }

    public void addSession(String deviceId, WebSocketSession session) {
        var result = map.putIfAbsent(deviceId, session);
        if(result == null)
            listenersMap.getOrDefault(deviceId, Collections.emptyList()).forEach(listener -> listener.onSessionAdded(deviceId, session));
    }

    public void deleteSession(String deviceId) {
        var result = map.remove(deviceId);
        if(result != null)
            listenersMap.getOrDefault(deviceId, Collections.emptyList()).forEach(listener -> listener.onSessionDeleted(deviceId));
    }
}
