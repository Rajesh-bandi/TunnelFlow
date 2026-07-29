package org.tunnelflow.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

@Service
@Slf4j
@Getter
public class TunnelConnectionManager {

    private final TunnelMessageReceiver receiver;
    private final ClientRegistrationService registrationService;
    private final ObjectMapper objectMapper;
    private final TunnelSender tunnelSender;
    private final TunnelCreationService tunnelCreationService;
    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;

    @Value("${tunnelflow.server.websocket-url}")
    private String serverUrl;

    private volatile TunnelWebSocketClient client;
    private volatile boolean intentionalDisconnect = false;

    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final long MAX_BACKOFF_MS = 30000;

    public TunnelConnectionManager(
            TunnelMessageReceiver receiver,
            ClientRegistrationService registrationService,
            ObjectMapper objectMapper,
            TunnelSender tunnelSender,
            TunnelCreationService tunnelCreationService,
            TunnelRuntimeRegistry tunnelRuntimeRegistry
    ) {
        this.receiver = receiver;
        this.registrationService = registrationService;
        this.objectMapper = objectMapper;
        this.tunnelSender = tunnelSender;
        this.tunnelCreationService = tunnelCreationService;
        this.tunnelRuntimeRegistry = tunnelRuntimeRegistry;
    }

    public void connect() {
        intentionalDisconnect = false;
        connectInternal();
    }

    private void connectInternal() {

        try {

            client = new TunnelWebSocketClient(
                    URI.create(serverUrl),
                    receiver,
                    objectMapper,
                    this::onUnexpectedDisconnect
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

    private void onUnexpectedDisconnect() {

        if (intentionalDisconnect) {
            log.info("Intentional disconnect — skipping reconnection.");
            return;
        }

        log.warn("Unexpected disconnect detected. Starting reconnection...");

        // Snapshot of tunnels to re-create after reconnection
        Collection<TunnelRuntime> tunnelsToRestore =
                new ArrayList<>(tunnelRuntimeRegistry.getAll());

        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {

            try {

                log.info("Reconnection attempt {}/{} in {}ms...",
                        attempt, MAX_RECONNECT_ATTEMPTS, backoffMs);

                Thread.sleep(backoffMs);

                connectInternal();

                log.info("Reconnected successfully. Re-creating {} tunnel(s)...",
                        tunnelsToRestore.size());

                // Re-create all tunnels that existed before disconnect
                for (TunnelRuntime oldTunnel : tunnelsToRestore) {

                    try {

                        TunnelCreatedResponse response =
                                tunnelCreationService.createTunnel(oldTunnel.getLocalPort());

                        // Remove old tunnel entry and register with new ID
                        tunnelRuntimeRegistry.remove(oldTunnel.getTunnelId());

                        TunnelRuntime newRuntime = TunnelRuntime.builder()
                                .tunnelId(response.getTunnelId())
                                .localPort(oldTunnel.getLocalPort())
                                .publicUrl(response.getPublicUrl())
                                .build();

                        tunnelRuntimeRegistry.register(newRuntime);

                        log.info("======================================");
                        log.info("Tunnel Re-created");
                        log.info("Port      : {}", oldTunnel.getLocalPort());
                        log.info("Old ID    : {}", oldTunnel.getTunnelId());
                        log.info("New ID    : {}", response.getTunnelId());
                        log.info("Public URL: {}", response.getPublicUrl());
                        log.info("======================================");

                    } catch (Exception e) {
                        log.error("Failed to re-create tunnel for port {}",
                                oldTunnel.getLocalPort(), e);
                    }
                }

                log.info("Reconnection complete.");
                return;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Reconnection interrupted.");
                return;
            } catch (Exception e) {
                log.error("Reconnection attempt {} failed: {}",
                        attempt, e.getMessage());
            }

            // Exponential backoff with cap
            backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
        }

        log.error("Failed to reconnect after {} attempts. Giving up.",
                MAX_RECONNECT_ATTEMPTS);
    }

    public void disconnect() {

        intentionalDisconnect = true;

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