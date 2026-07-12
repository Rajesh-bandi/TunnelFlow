package org.tunnelflow.tunnelflowserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ClientManager {

    private final Map<String, WebSocketSession> clients =
            new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionToClient =
            new ConcurrentHashMap<>();
    public void register(String clientId, WebSocketSession session) {
        clients.put(clientId, session);
        sessionToClient.put(session, clientId);
        log.info("Registered client {}", clientId);
    }

    public String getClientId(WebSocketSession session) {
        return sessionToClient.get(session);
    }

    public void unregister(WebSocketSession session) {

        String clientId = sessionToClient.remove(session);

        if (clientId != null) {
            clients.remove(clientId);
            log.info("Unregistered client {}", clientId);
        }
    }

    public WebSocketSession getSession(String clientId) {
        return clients.get(clientId);
    }

    public boolean isConnected(String clientId) {
        WebSocketSession session = clients.get(clientId);
        return session != null && session.isOpen();
    }

    public int getConnectedClientCount() {
        return clients.size();
    }
}