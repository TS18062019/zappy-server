package com.tsc.zappy.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadExecutor {

    @Bean(destroyMethod = "shutdown")
    ExecutorService executor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
