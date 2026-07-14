package org.tunnelflow.client.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.service.TunnelProtocolService;
import org.tunnelflow.client.service.TunnelSender;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class PingMessageHandler implements MessageHandler {

    private final TunnelSender tunnelSender;
    private final TunnelProtocolService protocolService;

    @Override
    public MessageType getSupportedType() {
        return MessageType.PING;
    }

    @Override
    public void handle(TunnelMessage message) throws Exception {

        log.info("Received PING [{}]", message.getRequestId());

        TunnelMessage pongMessage =
                protocolService.createPongMessage(message.getRequestId());

        tunnelSender.send(pongMessage);
    }
}