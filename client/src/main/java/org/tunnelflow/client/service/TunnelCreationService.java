package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class TunnelCreationService {
    private final TunnelProtocolService tunnelProtocolService;

    private final PendingTunnelRequestManager pendingTunnelRequestManager;

    private final TunnelSender tunnelSender;


    public TunnelCreatedResponse createTunnel(int port) throws JsonProcessingException {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<TunnelCreatedResponse> future =
                pendingTunnelRequestManager.register(requestId);
        TunnelMessage message =
                tunnelProtocolService.createCreateTunnelMessage(
                        requestId,
                        port
                );

        tunnelSender.send(message);
        log.info("Creating tunnel for port {}", port);

        try {

            TunnelCreatedResponse response = future.get();

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create tunnel",
                    e
            );

        }
    }

}