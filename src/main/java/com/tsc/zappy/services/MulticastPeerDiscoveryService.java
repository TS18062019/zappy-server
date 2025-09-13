package com.tsc.zappy.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.components.MulticastProperties;
import com.tsc.zappy.components.PeerMapProvider;
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
    private final HmacUtil hmacUtil;
    private final PeerMapProvider peerMapProvider;
    private final ObjectMapper objectMapper;

    public void beginAnnounce() {
        InetSocketAddress addr = new InetSocketAddress(mProperties.getMulticastAddr(), mProperties.getMulticasrPort());
        long announceInterval = mProperties.getAnnounceIntervalMs();
        try {
            log.info("Beginning broadcast. Pinging every {} ms...", announceInterval);
            while (!Thread.currentThread().isInterrupted() && channel.isOpen()) {
                DatagramFormat dgf = new DatagramFormat(hwInfo.getDeviceId(), hwInfo.getHostName(), hwInfo.getServerAddress());
                hmacUtil.signDatagram(dgf);
                ByteBuffer buf = ByteBuffer.wrap(objectMapper.writeValueAsString(dgf).getBytes());
                channel.send(buf, addr);
                Thread.sleep(announceInterval);
            }
        } catch (IOException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.info("Announce interrupted...");
            Thread.currentThread().interrupt();
        }
    }

    private void beginListen() {
        log.info("Listening for peers...");
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

    public void listDevices(WebSocketSession session) {
        final long refreshInterval = mProperties.getPeersRefreshInterval();
        var peerMap = peerMapProvider.getPeerMap();
        try {
            while (!Thread.currentThread().isInterrupted() && channel.isOpen()) {
                long currentTime = System.currentTimeMillis();
                peerMap.entrySet().removeIf(
                        entry -> currentTime - entry.getValue().getTimestamp() >= mProperties.getNoResponseTimeOut());
                peerMap.forEach((k, v) -> log.info("{}, {}", v.getName(), v.getIpAddr()));
                log.info("{} device(s) found", peerMap.size());
                if(session.isOpen())
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(peerMap)));
                Thread.sleep(refreshInterval);
            }
        } catch (IOException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.info("Listing interrupted...");
            Thread.currentThread().interrupt();
        }
    }

    @PostConstruct
    public void begin() {
        service.execute(this::beginListen);
    }

    /**
     * 
     * If a peer is new, add it otherwise update it's timestamp. If it's not heard
     * from within timeout, remove it.
     */
    private void updateMap(DatagramFormat dgf) {
        var peerMap = peerMapProvider.getPeerMap();
        long currentTime = System.currentTimeMillis();
        peerMap.merge(dgf.getDeviceId(), new PeerInfo(dgf.getName(), dgf.getAddress(), currentTime), (oldVal, newVal) -> {
            oldVal.setTimestamp(currentTime);
            return oldVal;
        });
    }
}
