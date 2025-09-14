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

    public Optional<WebSocketSession> getWebSocketSessionWithId(String deviceId) {
        return Optional.ofNullable(map.get(deviceId));
    }

    public Optional<WebSocketSession> getWebSocketSessionWithIp(String deviceIp) {
        return Optional.ofNullable(map.get(deviceIp));
    }

    public void addSession(String deviceIdOrIp, WebSocketSession session) {
        var result = map.putIfAbsent(deviceIdOrIp, session);
        if(result == null)
            listenersMap.getOrDefault(deviceIdOrIp, Collections.emptyList()).forEach(listener -> listener.onSessionAdded(deviceIdOrIp, session));
    }

    public void deleteSession(String deviceIdOrIp) {
        var result = map.remove(deviceIdOrIp);
        if(result != null)
            listenersMap.getOrDefault(deviceIdOrIp, Collections.emptyList()).forEach(listener -> listener.onSessionDeleted(deviceIdOrIp));
    }
}
