package org.tunnelflow.tunnelflowserver.service;

import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpResponseMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingRequestManager {

    private final ConcurrentHashMap<String, CompletableFuture<HttpResponseMessage>> pendingRequests =
            new ConcurrentHashMap<>();

    public CompletableFuture<HttpResponseMessage> register(String requestId) {

        CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void complete(String requestId, HttpResponseMessage response) {

        CompletableFuture<HttpResponseMessage> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.complete(response);
        }

    }

    public void fail(String requestId, Throwable throwable) {

        CompletableFuture<HttpResponseMessage> future =
                pendingRequests.remove(requestId);

        if (future != null) {
            future.completeExceptionally(throwable);
        }

    }

}