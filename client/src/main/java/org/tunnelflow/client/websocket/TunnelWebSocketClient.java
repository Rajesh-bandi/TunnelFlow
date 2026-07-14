package org.tunnelflow.client.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.tunnelflow.client.service.ClientRegistrationService;
import org.tunnelflow.client.service.TunnelMessageReceiver;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.net.URI;

@Slf4j
public class TunnelWebSocketClient extends WebSocketClient {

    private final TunnelMessageReceiver receiver;
    private final ClientRegistrationService registrationService;
    private final ObjectMapper objectMapper;

    public TunnelWebSocketClient(
            URI serverUri,
            TunnelMessageReceiver receiver,
            ClientRegistrationService registrationService,
            ObjectMapper objectMapper
    ) {
        super(serverUri);
        this.receiver = receiver;
        this.registrationService = registrationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

        log.info("Connected to tunnel server");

        registrationService.register();
    }

    @Override
    public void onMessage(String json) {
        receiver.receive(json);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection closed. Code: {}, Reason: {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket Error", ex);
    }

    public void send(TunnelMessage message) {

        try {

            super.send(objectMapper.writeValueAsString(message));

        } catch (JsonProcessingException e) {

            throw new RuntimeException("Failed to serialize TunnelMessage", e);

        }

    }
}