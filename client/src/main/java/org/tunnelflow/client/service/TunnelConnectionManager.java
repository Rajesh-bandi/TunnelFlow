package org.tunnelflow.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class TunnelConnectionManager {

    private final TunnelMessageReceiver receiver;
    private final ClientRegistrationService registrationService;
    private final ObjectMapper objectMapper;
    private final TunnelSender tunnelSender;

    @Value("${tunnelflow.server.websocket-url}")
    private String serverUrl;

    private TunnelWebSocketClient client;

    public void connect() {

        try {

            client = new TunnelWebSocketClient(
                    URI.create(serverUrl),
                    receiver,
                    registrationService,
                    objectMapper
            );

            // Give the sender access to the live WebSocket client
            tunnelSender.attach(client);

            log.info("Connecting to {}", serverUrl);

            client.connectBlocking();

            registrationService.register();

        } catch (Exception e) {

            throw new RuntimeException("Failed to connect to tunnel server", e);

        }
    }

    public void disconnect() {

        // Remove the reference from TunnelSender
        tunnelSender.detach();

        if (client != null) {
            client.close();
        }

    }

    public boolean isConnected() {

        return client != null && client.isOpen();

    }

}