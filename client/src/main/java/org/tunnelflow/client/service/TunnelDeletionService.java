package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.tunnel.TunnelDeletedResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TunnelDeletionService {

    private final TunnelProtocolService tunnelProtocolService;
    private final PendingTunnelDeletionManager pendingTunnelDeletionManager;
    private final TunnelSender tunnelSender;

    public TunnelDeletedResponse deleteTunnel(String tunnelId) throws JsonProcessingException {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<TunnelDeletedResponse> future = pendingTunnelDeletionManager.register(requestId);

        TunnelMessage message = tunnelProtocolService.createDeleteTunnelMessage(requestId, tunnelId);
        tunnelSender.send(message);

        log.info("Deleting tunnel [{}]", tunnelId);

        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingTunnelDeletionManager.fail(requestId, e);
            log.warn("Tunnel [{}] deletion server response timed out after 3s (cleaning up local runtime)", tunnelId);
            return null;
        } catch (Exception e) {
            pendingTunnelDeletionManager.fail(requestId, e);
            log.warn("Failed to delete tunnel [{}] remotely: {}", tunnelId, e.getMessage());
            return null;
        }
    }
}