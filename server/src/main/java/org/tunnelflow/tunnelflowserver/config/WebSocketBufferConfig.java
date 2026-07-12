package org.tunnelflow.tunnelflowserver.config;

import jakarta.websocket.server.ServerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketBufferConfig {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {

        ServletServerContainerFactoryBean container =
                new ServletServerContainerFactoryBean();

        // 1 MB
        container.setMaxTextMessageBufferSize(1024 * 1024);

        // 1 MB
        container.setMaxBinaryMessageBufferSize(1024 * 1024);

        return container;
    }
}