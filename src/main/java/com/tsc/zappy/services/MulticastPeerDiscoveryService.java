package com.tsc.zappy.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.components.MulticastProperties;
import com.tsc.zappy.controller.UIWebSocketController;
import com.tsc.zappy.dto.DatagramFormat;
import com.tsc.zappy.dto.PeerInfo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MulticastPeerDiscoveryService {

    private final DatagramChannel channel;
    private final ExecutorService service;
    private final HardwareInfo hwInfo;
    private final MulticastProperties mProperties;
    private final UIWebSocketController uiWebSocketController;
    private final HmacUtil hmacUtil;
    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    private void beginAnnounce() {
        InetSocketAddress addr = new InetSocketAddress(mProperties.getMulticastAddr(), mProperties.getMulticasrPort());
        try {
            while (channel.isOpen()) {
                DatagramFormat dgf = new DatagramFormat(hmacUtil.getNonce(), hwInfo.getHostName(), hwInfo.getServerAddress());
                hmacUtil.signDatagram(dgf);
                ByteBuffer buf = ByteBuffer.wrap(objectMapper.writeValueAsString(dgf).getBytes());
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
        ByteBuffer buf = ByteBuffer.allocate(192);
        while (channel.isOpen()) {
            try {
                buf.clear();
                channel.receive(buf);
                buf.flip();
                String rcvd = new String(buf.array(), 0, buf.limit());
                var dgf = objectMapper.readValue(rcvd, DatagramFormat.class);
                if(hmacUtil.verifyDatagram(dgf))
                    updateMap(dgf);
                else 
                    log.info("Device {}, Ip: {} rejected due to HMAC verification failure", dgf.getName(), dgf.getAddress());
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
                peerMap.forEach((k, v) -> log.info("{}, {}", k, v.getName()));
                uiWebSocketController.sendToUser(peerMap);
                Thread.sleep(mProperties.getPeersRefreshInterval());
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
    private void updateMap(DatagramFormat dgf) {
        if(hwInfo.getServerAddress().equals(dgf.getAddress()))
            return;
        long currentTime = System.currentTimeMillis();
        peerMap.merge(dgf.getAddress(), new PeerInfo(dgf.getName(), currentTime), (oldVal, newVal) -> {
            oldVal.setTimestamp(currentTime);
            return oldVal;
        });
    }
}
