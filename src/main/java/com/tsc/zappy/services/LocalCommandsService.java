package com.tsc.zappy.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.tsc.zappy.components.WebSocketSessionProvider;
import com.tsc.zappy.dto.WebSocketSessionDTO;
import com.tsc.zappy.dto.WebSocketTextMessageDTO;
import com.tsc.zappy.interfaces.SessionChangeListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class LocalCommandsService implements SessionChangeListener {

    private final MulticastPeerDiscoveryService peerDiscoveryService;
    private final WebSocketSessionProvider sessionProvider;
    private final ExecutorService executorService;

    private List<Future<?>> taskList = new ArrayList<>(2);

    /**
     * 
     * @param dto
     * @param session
     */
    public void processCommand(WebSocketTextMessageDTO dto, WebSocketSession session) {
        if ("DISCOVER_PEERS".equals(dto.getCommand())) {
            if (taskList.isEmpty()) {
                log.info("Discovering peers...");
                sessionProvider.registerListener(this, session.getId());
                taskList.add(executorService.submit(peerDiscoveryService::beginAnnounceOnListen));
                taskList.add(executorService.submit(() -> peerDiscoveryService.listDevices(session)));
            }
        } else if ("STOP_DISCOVERY".equals(dto.getCommand())) {
            stopAll(session.getId());
        }
    }

    public void stopAll(String sessionId) {
        log.info("Discovery stopped");
        for (var task : taskList)
            task.cancel(true);
        taskList.clear();
        sessionProvider.unregisterListener(this, sessionId);
    }

    @Override
    public void onSessionDeleted(String sessionId) {
       stopAll(sessionId);
    }

    @Override
    public void onSessionAdded(String sessionId, WebSocketSessionDTO sessionDTO) {
        // not needed
    }

   
}
