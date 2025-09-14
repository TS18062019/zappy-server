package com.tsc.zappy.interceptors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.constants.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class DeviceHandshakeInterceptor implements HandshakeInterceptor {

    private final HardwareInfo info;
    private final HmacUtil hmacUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        try {
            log.info("Handler ID beforeHandshake: {}", wsHandler.toString());
            String query = request.getURI().getQuery();
            String deviceId = getQueryParam(query).get(Constants.DEVICE_ID);
            String signature = getQueryParam(query).get(Constants.SIGN);
            if (!verificationSuccess(deviceId, signature))
                throw new IllegalAccessError("Bad credentials");
            InetAddress addr = request.getRemoteAddress().getAddress();
            if (addr != null) {
                log.info("Handshake authorized for device {}, {}", addr.getHostName(), addr.getHostAddress());
                attributes.put(Constants.DEVICE_ID, deviceId);
                return true;
            } else
                throw new IllegalAccessError("Could not verify your device!");
        } catch (Exception e) {
            log.info("Handshake denied", e);
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
                // implementation not needed yet
                log.info("Handler ID afterHandshake: {}", wsHandler.toString());
                try {
                    response.getBody().write(info.getDeviceId().getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
    }

    private Map<String, String> getQueryParam(String query) {
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("([^&=]+)=([^&]*)");
        Matcher m = p.matcher(query);
        while (m.find()) {
            map.put(
                    URLDecoder.decode(m.group(1), StandardCharsets.UTF_8),
                    URLDecoder.decode(m.group(2), StandardCharsets.UTF_8));
        }
        return map;
    }

    private boolean verificationSuccess(String deviceId, String hMac) {
        return true;
        //return hmacUtil.verify(deviceId, hMac);
    }

}
