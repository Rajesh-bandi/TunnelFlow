package org.tunnelflow.client.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PendingRegistrationManager {

    private CompletableFuture<String> registrationFuture;

    public CompletableFuture<String> register() {

        registrationFuture = new CompletableFuture<>();

        return registrationFuture;
    }

    public void complete(String clientId) {

        if (registrationFuture != null) {
            registrationFuture.complete(clientId);
            registrationFuture = null;
        }

    }

    public void fail(Throwable throwable) {

        if (registrationFuture != null) {
            registrationFuture.completeExceptionally(throwable);
            registrationFuture = null;
        }

    }

}