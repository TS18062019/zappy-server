package com.tsc.zappy.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.tsc.zappy.components.WebSocketSessionProvider;
import com.tsc.zappy.constants.Constants;
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
        String sourceDeviceId = (String) session.getAttributes().get(Constants.DEVICE_ID);
        if ("DISCOVER_PEERS".equals(dto.getMsgData())) {
            if (taskList.isEmpty()) {
                log.info("Discovering peers...");
                sessionProvider.registerListener(this, sourceDeviceId);
                taskList.add(executorService.submit(peerDiscoveryService::beginAnnounce));
                taskList.add(executorService.submit(() -> peerDiscoveryService.listDevices(session)));
            }
        } else if ("STOP_DISCOVERY".equals(dto.getMsgData())) {
            stopAll(dto.getDestinationDeviceId());
        }
    }

    public void stopAll(String deviceId) {
        log.info("Discovery stopped");
        for (var task : taskList)
            task.cancel(true);
        taskList.clear();
        sessionProvider.unregisterListener(this, deviceId);
    }

    @Override
    public void onSessionAdded(String deviceId, WebSocketSession session) {
        // not needed
    }

    @Override
    public void onSessionDeleted(String deviceId) {
       stopAll(deviceId);
    }

   
}
