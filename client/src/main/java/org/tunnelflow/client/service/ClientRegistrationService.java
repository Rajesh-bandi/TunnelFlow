package org.tunnelflow.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.client.ClientRegisterRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationService {
    private final PendingRegistrationManager pendingRegistrationManager;
    private final TunnelSender tunnelSender;
    private final ObjectMapper objectMapper;

    public void register() {
        CompletableFuture<String> future =
                pendingRegistrationManager.register();

        try {

            ClientRegisterRequest request =
                    ClientRegisterRequest.builder()
                            .machineName(
                                    Optional.ofNullable(System.getenv("COMPUTERNAME"))
                                            .orElse(System.getenv("HOSTNAME"))
                            )
                            .os(System.getProperty("os.name"))
                            .version("1.0.0")
                            .build();

            TunnelMessage message =
                    TunnelMessage.builder()
                            .type(MessageType.CLIENT_REGISTER)
                            .payload(objectMapper.writeValueAsString(request))
                            .build();

            tunnelSender.send(message);

            log.info("CLIENT_REGISTER sent");
            String clientId = future.get();

            log.info("Client [{}] registered successfully.", clientId);

        } catch (Exception e) {

            log.error("Failed to register client", e);

        }
    }
}