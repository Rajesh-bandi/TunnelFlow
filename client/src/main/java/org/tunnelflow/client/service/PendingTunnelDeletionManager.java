package org.tunnelflow.client.service;

import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.tunnel.TunnelDeletedResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingTunnelDeletionManager {

    private final ConcurrentHashMap<
            String,
            CompletableFuture<TunnelDeletedResponse>
            > pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<TunnelDeletedResponse> register(
            String requestId
    ) {

        CompletableFuture<TunnelDeletedResponse> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void complete(
            String requestId,
            TunnelDeletedResponse response
    ) {

        CompletableFuture<TunnelDeletedResponse> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.complete(response);
        }
    }

    public void fail(
            String requestId,
            Throwable throwable
    ) {

        CompletableFuture<TunnelDeletedResponse> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.completeExceptionally(throwable);
        }
    }
}