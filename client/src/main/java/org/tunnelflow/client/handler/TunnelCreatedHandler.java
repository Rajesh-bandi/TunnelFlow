package org.tunnelflow.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.PendingTunnelRequestManager;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;


@Component
@RequiredArgsConstructor
@Slf4j
public class TunnelCreatedHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final PendingTunnelRequestManager pendingTunnelRequestManager;

    @Override
    public MessageType getSupportedType() {
        return MessageType.TUNNEL_CREATED;
    }

    @Override
    public void handle(TunnelMessage message) throws Exception {

        TunnelCreatedResponse response =
                objectMapper.readValue(
                        message.getPayload(),
                        TunnelCreatedResponse.class
                );
        pendingTunnelRequestManager.complete(
                message.getRequestId(),
                response
        );
        log.info("======================================");
        log.info("Tunnel Created");
        log.info("Tunnel ID : {}", response.getTunnelId());
        log.info("Public URL: {}", response.getPublicUrl());
        log.info("======================================");
    }
}