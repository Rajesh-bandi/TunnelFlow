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

        container.setMaxTextMessageBufferSize(1024 * 1024);   // 1 MB — enough for typical HTTP payloads
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1 MB

        return container;
    }
}