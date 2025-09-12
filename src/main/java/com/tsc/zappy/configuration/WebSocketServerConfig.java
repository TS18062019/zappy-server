package com.tsc.zappy.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.tsc.zappy.handlers.BinaryDataHandler;
import com.tsc.zappy.handlers.TextDataHandler;
import com.tsc.zappy.interceptors.DeviceHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketServerConfig implements WebSocketConfigurer {

    private final BinaryDataHandler bHandler;
    private final TextDataHandler tHandler;

    private final DeviceHandshakeInterceptor deviceHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        
        registry.addHandler(bHandler, "/ws/file")
        .addInterceptors(deviceHandshakeInterceptor)
        .setAllowedOrigins("*");
        
        registry.addHandler(tHandler, "/ws/text")
        .addInterceptors(deviceHandshakeInterceptor)
        .setAllowedOrigins("*");
    }
    
}
