package com.tsc.zappy.components;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@Getter
public class HardwareInfo {

    private String hostName;
    private String serverIp;
    private String serverAddress;
    private String deviceId;
    private NetworkInterface nif;

    @Value("${server.port}")
    private int serverPort;

    @PostConstruct
    public void init() {
        try {
            nif = getInterface();
            serverIp = determineServerIp(nif);
            deviceId = UUID.randomUUID().toString();
            hostName = InetAddress.getLocalHost().getHostName();
            serverAddress = "http://" + serverIp + ":" + serverPort;
            log.info("Device: {}, Id: {} hosted at {}:{} using interface {}", hostName, deviceId, serverIp,
                    serverPort, nif.getDisplayName());
        } catch (SocketException | UnknownHostException e) {
            log.error("Error while getting hardware info. Application cannot proceed.", e);
            throw new IllegalStateException();
        }
    }

    private String determineServerIp(NetworkInterface nif) {
        String ip = "localhost";
        var addresses = nif.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (addr instanceof Inet4Address)
                return addr.getHostAddress();
        }
        return ip;
    }

    private NetworkInterface getInterface() throws SocketException {
        var x = NetworkInterface.getNetworkInterfaces();
        while (x.hasMoreElements()) {
            var y = x.nextElement();
            if (!y.isLoopback() && y.isUp() && y.supportsMulticast()) {
                log.info("Found network interface {}", y.getDisplayName());
                return y;
            }
        }
        return null;
    }
}
