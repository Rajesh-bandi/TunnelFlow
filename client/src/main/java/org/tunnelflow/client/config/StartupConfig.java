package org.tunnelflow.client.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tunnelflow.client.service.TunnelConnectionManager;


@Configuration
public class StartupConfig {

    @Bean
    CommandLineRunner connectToServer(TunnelConnectionManager tunnelConnectionManager) {
        return args -> {
            tunnelConnectionManager.connect();
        };
    }
}