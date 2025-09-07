package com.tsc.zappy.components;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class HardwareInfo {

    private String localMacAddr;
    private String hostName;
    private NetworkInterface nif;

    public HardwareInfo() {
        try {
            var x = NetworkInterface.getNetworkInterfaces();
            while (x.hasMoreElements()) {
                var y = x.nextElement();
                if (!y.isLoopback() && y.isUp() && y.supportsMulticast()) {
                    nif = y;
                    log.info("Found network interface {}", y.getDisplayName());
                    break;
                }
            }
            if (nif != null) {
                byte[] bArr = nif.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (byte b : bArr)
                    sb.append(String.format("%02X", b));
                localMacAddr = sb.toString();
                log.info("Device MAC: {}", localMacAddr);
            } else
                localMacAddr = "UNKNOWN";
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (SocketException e) {
            log.error(e);
            localMacAddr = "ERROR";
            hostName = "ERROR";
        } catch(UnknownHostException e) {
            log.error(e);
            hostName = "UNKNOWN";
        }
    }

    public String getLocalMacAddr() {
        return localMacAddr;
    }

    public NetworkInterface getNif() {
        return nif;
    }

    public String getHostName() {
        return hostName;
    }

}
