package com.tsc.zappy.components;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.tsc.zappy.dto.WebSocketSessionDTO;
import com.tsc.zappy.interfaces.SessionChangeListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class WebSocketSessionProvider {

    private Map<String, List<SessionChangeListener>> listenersMap = new ConcurrentHashMap<>();
    private Map<String, WebSocketSessionDTO> map = new ConcurrentHashMap<>();

    /**
     * Registers session change listeners for a particular device
     * 
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

    public Optional<WebSocketSessionDTO> getWebSocketSession(String sessionId) {
        return Optional.ofNullable(map.get(sessionId));
    }

    public Optional<WebSocketSession> getWebSocketSessionWithDeviceId(String deviceId) {
        return map.values().stream().filter(dto -> deviceId.equals(dto.getDeviceId())).findFirst()
                .map(WebSocketSessionDTO::getSession);
    }

    public Optional<WebSocketSession> getWebSocketSessionWithDeviceIp(String deviceIp) {
        return map.values().stream().filter(dto -> deviceIp.equals(dto.getDeviceIp())).findFirst()
                .map(WebSocketSessionDTO::getSession);
    }

    public void addSession(String sessionId, WebSocketSessionDTO sessionDto) {
        var result = map.putIfAbsent(sessionId, sessionDto);
        if (result == null)
            listenersMap.getOrDefault(sessionId, Collections.emptyList())
                    .forEach(listener -> listener.onSessionAdded(sessionId, sessionDto));
    }

    public void deleteSession(String sessionId) {
        var result = map.remove(sessionId);
        if (result != null)
            listenersMap.getOrDefault(sessionId, Collections.emptyList())
                    .forEach(listener -> listener.onSessionDeleted(sessionId));
    }
}
