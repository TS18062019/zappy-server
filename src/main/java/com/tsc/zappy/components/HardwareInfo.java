package com.tsc.zappy.components;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@Getter
public class HardwareInfo {

    private String localMacAddr;
    private String hostName;
    private String serverIp;
    private String serverAddress;
    private NetworkInterface nif;

    @Value("${server.port}")
    private int serverPort;

    @PostConstruct
    public void init() {
        try {
            nif = getInterface();
            serverIp = determineServerIp(nif);
            localMacAddr = getMacAddr(nif);
            hostName = InetAddress.getLocalHost().getHostName();
            serverAddress = "http://" + serverIp + ":" + serverPort;
            log.info("Device: {}, MAC: {} hosted at {}:{} using interface {}", hostName, localMacAddr, serverIp,
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

    private String getMacAddr(NetworkInterface nif) throws SocketException {
        if (nif != null) {
            byte[] bArr = nif.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (byte b : bArr)
                sb.append(String.format("%02X", b));
            log.info("Device MAC: {}", localMacAddr);
            return sb.toString();
        }
        return "UNKNOWN";
    }
}
