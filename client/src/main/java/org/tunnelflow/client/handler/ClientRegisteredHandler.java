package org.tunnelflow.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.PendingRegistrationManager;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.client.ClientRegisteredResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRegisteredHandler implements MessageHandler {
    private final PendingRegistrationManager pendingRegistrationManager;
    private final ObjectMapper objectMapper;

    @Override
    public MessageType getSupportedType() {
        return MessageType.CLIENT_REGISTERED;
    }

    @Override
    public void handle(TunnelMessage message) throws Exception {

        ClientRegisteredResponse response =
                objectMapper.readValue(
                        message.getPayload(),
                        ClientRegisteredResponse.class
                );
        pendingRegistrationManager.complete(
                response.getClientId()
        );

        log.info("======================================");
        log.info("TunnelFlow Connected");
        log.info("Client ID : {}", response.getClientId());
        log.info("======================================");
    }
}