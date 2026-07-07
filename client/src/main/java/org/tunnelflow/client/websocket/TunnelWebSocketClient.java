package org.tunnelflow.client.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.tunnelflow.client.service.TunnelMessageReceiver;

import java.net.URI;

@Slf4j
public class TunnelWebSocketClient extends WebSocketClient {
    private final TunnelMessageReceiver receiver;
    public TunnelWebSocketClient(URI serverUri,TunnelMessageReceiver receiver) {
        super(serverUri);
        this.receiver = receiver;
    }


    @Override
    public void onOpen(ServerHandshake handshake) {

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
}