package org.tunnelflow.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.PendingTunnelDeletionManager;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.tunnel.TunnelDeletedResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class TunnelDeletedHandler implements MessageHandler {

    private final ObjectMapper objectMapper;

    private final PendingTunnelDeletionManager
            pendingTunnelDeletionManager;

    @Override
    public MessageType getSupportedType() {
        return MessageType.TUNNEL_DELETED;
    }

    @Override
    public void handle(TunnelMessage message) throws Exception {

        TunnelDeletedResponse response =
                objectMapper.readValue(
                        message.getPayload(),
                        TunnelDeletedResponse.class
                );

        pendingTunnelDeletionManager.complete(
                message.getRequestId(),
                response
        );

        log.info(
                "Tunnel [{}] deleted",
                response.getTunnelId()
        );
    }
}