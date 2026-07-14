package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;
import org.tunnelflow.protocol.protocol.TunnelMessage;

@Service
@Slf4j
public class TunnelSender {

    private volatile TunnelWebSocketClient client;

    public void attach(TunnelWebSocketClient client) {
        this.client = client;
    }

    public void detach() {
        this.client = null;
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }

    public void send(TunnelMessage message) {

        if (!isConnected()) {
            log.warn("Tunnel not connected. Dropping message [{}]",
                    message.getRequestId());
            return;
        }

        client.send(message);
    }
}