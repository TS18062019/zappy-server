package com.tsc.zappy.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.components.MulticastProperties;
import com.tsc.zappy.components.PeerMapProvider;
import com.tsc.zappy.constants.Constants;
import com.tsc.zappy.dto.DatagramFormat;
import com.tsc.zappy.dto.PeerInfo;
import com.tsc.zappy.dto.WebSocketTextMessageResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MulticastPeerDiscoveryService {

    private final DatagramChannel channel;
    private final HardwareInfo hwInfo;
    private final MulticastProperties mProperties;
    private final HmacUtil hmacUtil;
    private final PeerMapProvider peerMapProvider;
    private final ObjectMapper objectMapper;

    private void announce(DatagramFormat dgf, InetSocketAddress addr) {
        try {
            hmacUtil.signDatagram(dgf);
            channel.send(ByteBuffer.wrap(objectMapper.writeValueAsString(dgf).getBytes()), addr);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void beginAnnounceOnListen() {
        log.info("Listening for peers...");
        InetSocketAddress addr = new InetSocketAddress(mProperties.getMulticastAddr(), mProperties.getMulticasrPort());
        DatagramFormat broadcastDgf = new DatagramFormat(hwInfo.getDeviceId(), hwInfo.getHostName(),
                hwInfo.getServerIp());
        ByteBuffer buf = ByteBuffer.allocate(256);
        long announceInterval = mProperties.getAnnounceIntervalMs();
        while (!Thread.currentThread().isInterrupted() && channel.isOpen()) {
            try {
                buf.clear();
                // non-blocking call
                if (channel.receive(buf) != null) {
                    buf.flip();
                    String rcvd = StandardCharsets.UTF_8.decode(buf).toString();
                    var dgf = objectMapper.readValue(rcvd, DatagramFormat.class);
                    if (hmacUtil.verifyDatagram(dgf)) {
                        updateMap(dgf);
                    } else
                        log.info("Device {}, Ip: {} rejected due to HMAC verification failure", dgf.getName(),
                                dgf.getAddress());
                }
                // announce self presence
                announce(broadcastDgf, addr);
                Thread.sleep(announceInterval);
            } catch (IOException e) {
                if (!(e instanceof AsynchronousCloseException))
                    log.error(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Stopping discovery...");
    }

    public void listDevices(WebSocketSession session) {
        final long refreshInterval = mProperties.getPeersRefreshInterval();
        var peerMap = peerMapProvider.getPeerMap();
        int lastRecordedSize = -1;
        try {
            while (!Thread.currentThread().isInterrupted() && channel.isOpen()) {
                long currentTime = System.currentTimeMillis();
                peerMap.entrySet().removeIf(
                        entry -> currentTime - entry.getValue().getTimestamp() >= mProperties.getNoResponseTimeOut());
                peerMap.forEach((k, v) -> log.info("{}, {}", v.getName(), v.getIpAddr()));
                if (lastRecordedSize != peerMap.size())
                    log.info("{} device(s) found", peerMap.size());
                lastRecordedSize = peerMap.size();
                if (session.isOpen())
                    session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(new WebSocketTextMessageResponseDTO(Constants.SUCCESS,
                                    Constants.RESPONSE, Map.of("peerMap", peerMap)))));
                Thread.sleep(refreshInterval);
            }
        } catch (IOException e) {
            log.error(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Stopping listing...");
    }

    /**
     * 
     * If a peer is new, add it otherwise update it's timestamp. If it's not heard
     * from within timeout, remove it.
     */
    private void updateMap(DatagramFormat dgf) {
        var peerMap = peerMapProvider.getPeerMap();
        long currentTime = System.currentTimeMillis();
        peerMap.merge(dgf.getDeviceId(), new PeerInfo(dgf.getName(), dgf.getAddress(), currentTime),
                (oldVal, newVal) -> {
                    oldVal.setTimestamp(currentTime);
                    return oldVal;
                });
    }
}
