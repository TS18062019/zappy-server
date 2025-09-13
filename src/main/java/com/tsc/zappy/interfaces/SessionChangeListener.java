package com.tsc.zappy.interfaces;

import org.springframework.web.socket.WebSocketSession;

public interface SessionChangeListener {
    
    void onSessionAdded(String deviceId, WebSocketSession session);
    void onSessionDeleted(String deviceId);
}
