package org.tunnelflow.client.service;

import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingTunnelRequestManager {

    private final ConcurrentHashMap<String, CompletableFuture<TunnelCreatedResponse>>
            pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<TunnelCreatedResponse> register(String requestId) {

        CompletableFuture<TunnelCreatedResponse> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void complete(
            String requestId,
            TunnelCreatedResponse response
    ) {

        CompletableFuture<TunnelCreatedResponse> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.complete(response);
        }

    }

    public void fail(
            String requestId,
            Throwable throwable
    ) {

        CompletableFuture<TunnelCreatedResponse> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.completeExceptionally(throwable);
        }

    }

}