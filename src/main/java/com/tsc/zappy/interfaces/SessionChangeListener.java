package com.tsc.zappy.interfaces;

import com.tsc.zappy.dto.WebSocketSessionDTO;

public interface SessionChangeListener {
    
    void onSessionAdded(String sessionId, WebSocketSessionDTO sessionDTO);
    void onSessionDeleted(String sessionId);
}
