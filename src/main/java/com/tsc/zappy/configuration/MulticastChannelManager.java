package com.tsc.zappy.configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.MulticastProperties;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
@AllArgsConstructor
public class MulticastChannelManager {

    private MulticastProperties mProperties;
    private HardwareInfo hwInfo;

    @Bean(destroyMethod = "close")
    DatagramChannel channel() {
        NetworkInterface nif = hwInfo.getNif();
        if (nif == null) {
            log.error("No network interface supports multicast!");
            throw new IllegalStateException();
        }
        try {
            DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, nif)
                    .setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false)
                    .bind(new InetSocketAddress(mProperties.getMulticasrPort()));
            InetAddress multicastGroup = InetAddress.getByName(mProperties.getMulticastAddr());
            channel.join(multicastGroup, nif);
            channel.configureBlocking(false);
            return channel;
        } catch (IOException e) {
            log.error(e);
            throw new IllegalStateException();
        }
    }
}
