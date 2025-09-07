package com.tsc.zappy.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.springframework.stereotype.Service;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.MulticastProperties;
import com.tsc.zappy.controller.UIWebSocketController;
import com.tsc.zappy.dto.PeerInfo;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@AllArgsConstructor
public class MulticastPeerDiscoveryService {

    private DatagramChannel channel;
    private ExecutorService service;
    private HardwareInfo hwInfo;
    private MulticastProperties mProperties;
    private UIWebSocketController uiWebSocketController;

    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    private void beginAnnounce() {
        String message = String.format("%s[%s] alive", hwInfo.getHostName(), hwInfo.getLocalMacAddr());
        ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
        InetSocketAddress addr = new InetSocketAddress(mProperties.getMulticastAddr(), mProperties.getMulticasrPort());
        try {
            while (channel.isOpen()) {
                buf.rewind();
                channel.send(buf, addr);
                log.info("Broadcast sent!");
                Thread.sleep(mProperties.getAnnounceIntervalMs());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e);
        }
    }

    private void beginListen() {
        ByteBuffer buf = ByteBuffer.allocate(128);
        while (channel.isOpen()) {
            try {
                buf.clear();
                SocketAddress addr = channel.receive(buf);
                buf.flip();
                String rcvd = new String(buf.array(), 0, buf.limit());
                int idxA = rcvd.indexOf('[');
                String clientName = rcvd.substring(0, idxA);
                String clientMac = rcvd.substring(idxA + 1, rcvd.indexOf(']'));
                if (clientMac.equals(hwInfo.getLocalMacAddr()))
                    continue;
                updateMap(clientName, clientMac, addr.toString());
            } catch (IOException e) {
                if (!(e instanceof AsynchronousCloseException))
                    log.error(e);
            }
        }
    }

    private void listDevices() {
        try {
            while (channel.isOpen()) {
                long currentTime = System.currentTimeMillis();
                peerMap.entrySet().removeIf(
                        entry -> currentTime - entry.getValue().getTimestamp() >= mProperties.getNoResponseTimeOut());
                peerMap.forEach((k, v) -> log.info("{}, {}, {}", k, v.getName(),
                        v.getIp()));
                uiWebSocketController.sendToUser(peerMap);
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e);
        }
    }

    @PostConstruct
    public void begin() {
        // announce every interval
        service.execute(this::beginAnnounce);
        // listen for incoming messages
        service.execute(this::beginListen);
        // print connected devices
        service.execute(this::listDevices);
    }

    /**
     * 
     * If a peer is new, add it otherwise update it's timestamp. If it's not heard
     * from within timeout, remove it.
     */
    private void updateMap(String clientName, String clientMac, String ip) {
        long currentTime = System.currentTimeMillis();
        peerMap.merge(clientMac, new PeerInfo(clientName, ip, currentTime), (oldVal, newVal) -> {
            oldVal.setTimestamp(currentTime);
            return oldVal;
        });
    }
}
