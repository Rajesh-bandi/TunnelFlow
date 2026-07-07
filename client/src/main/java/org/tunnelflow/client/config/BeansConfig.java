package org.tunnelflow.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tunnelflow.client.service.TunnelMessageReceiver;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class BeansConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TunnelWebSocketClient tunnelWebSocketClient(
            TunnelMessageReceiver receiver,
            @Value("${tunnelflow.server.websocket-url}") String websocketUrl
    ) {

        return new TunnelWebSocketClient(
                URI.create(websocketUrl),
                receiver
        );
    }

    @Bean(destroyMethod = "")  // empty string = don't call any destroy method
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}
