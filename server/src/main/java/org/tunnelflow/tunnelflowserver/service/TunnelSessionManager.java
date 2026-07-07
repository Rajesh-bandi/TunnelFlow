package org.tunnelflow.tunnelflowserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TunnelSessionManager {
    private WebSocketSession session;
    private final ObjectMapper objectMapper;

    public void register(WebSocketSession session) {
        this.session = session;
    }

    public void send(TunnelMessage message) throws IOException {

        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("No client connected");
        }

        session.sendMessage(
                new TextMessage(objectMapper.writeValueAsString(message))
        );
    }

    public void unRegister(WebSocketSession session) {
        this.session = null;
    }
}
