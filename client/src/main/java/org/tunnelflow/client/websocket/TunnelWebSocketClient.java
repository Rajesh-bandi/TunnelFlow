package org.tunnelflow.client.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.tunnelflow.client.service.TunnelMessageReceiver;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.net.URI;
import java.nio.ByteBuffer;

@Slf4j
public class TunnelWebSocketClient extends WebSocketClient {

    private final TunnelMessageReceiver receiver;
    private final ObjectMapper objectMapper;
    private final Runnable onDisconnect;

    public TunnelWebSocketClient(
            URI serverUri,
            TunnelMessageReceiver receiver,
            ObjectMapper objectMapper,
            Runnable onDisconnect
    ) {
        super(serverUri);
        this.receiver = receiver;
        this.objectMapper = objectMapper;
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

        log.info("Connected to tunnel server");
    }

    @Override
    public void onMessage(String json) {
        receiver.receive(json);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] array = new byte[bytes.remaining()];
        bytes.get(array);
        receiver.receiveBinary(array);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Connection closed. Code: {}, Reason: {}, Remote: {}", code, reason, remote);

        if (remote && onDisconnect != null) {
            // Server/network killed the connection — trigger reconnection on a separate thread
            Thread.startVirtualThread(onDisconnect);
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket Error", ex);
    }

    /**
     * Synchronized to prevent concurrent WebSocket frame writes from
     * multiple threads (e.g. the HTTP forwarding thread pool).
     * Concurrent writes corrupt WebSocket frames and cause code 1006 disconnects.
     */
    public void send(TunnelMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            synchronized (this) {
                super.send(json);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TunnelMessage", e);
        }
    }

    public void sendBinary(byte[] bytes) {
        synchronized (this) {
            super.send(bytes);
        }
    }
}