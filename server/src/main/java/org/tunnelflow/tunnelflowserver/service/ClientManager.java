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

    public void register(String clientId, WebSocketSession session) {

        clients.put(clientId, session);

        log.info("Client [{}] connected", clientId);
    }

    public void unregister(String clientId) {

        clients.remove(clientId);

        log.info("Client [{}] disconnected", clientId);
    }

    public WebSocketSession getSession(String clientId) {

        return clients.get(clientId);
    }

    public boolean isConnected(String clientId) {

        WebSocketSession session = clients.get(clientId);

        return session != null && session.isOpen();
    }

    public int connectedClients() {

        return clients.size();
    }

}