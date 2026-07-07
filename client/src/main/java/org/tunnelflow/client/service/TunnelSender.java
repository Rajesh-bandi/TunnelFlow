package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;
import org.tunnelflow.protocol.protocol.TunnelMessage;

@Service
@Slf4j
public class TunnelSender {
    private final TunnelWebSocketClient client;
    private final ObjectMapper objectMapper;

    public TunnelSender(@Lazy TunnelWebSocketClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }
    public void send(TunnelMessage message) {
        if (client == null || !client.isOpen()) {
            throw new IllegalStateException("Tunnel client is not connected.");
        }

        try {
            client.send(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("failed to serialize message", e);
        }
    }
}