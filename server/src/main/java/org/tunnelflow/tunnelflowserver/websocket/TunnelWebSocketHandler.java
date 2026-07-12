package org.tunnelflow.tunnelflowserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.client.ClientRegisterRequest;
import org.tunnelflow.tunnelflowserver.model.TunnelInfo;
import org.tunnelflow.tunnelflowserver.service.*;

import java.util.UUID;

import static org.tunnelflow.protocol.protocol.MessageType.HTTP_RESPONSE;

@Slf4j
@Component
@RequiredArgsConstructor
public class TunnelWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final TunnelManager tunnelManager;
    private final ClientManager clientManager;
    private final TunnelProtocolService tunnelProtocolService;
    private final PendingRequestManager pendingRequestManager;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        log.info("WebSocket connected [{}], waiting for registration...",
                session.getId());

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        TunnelMessage tunnelMessage =
                objectMapper.readValue(message.getPayload(), TunnelMessage.class);

        switch (tunnelMessage.getType()) {

            case PONG -> {
                log.info("Received PONG [{}]", tunnelMessage.getRequestId());
            }

            case HTTP_RESPONSE -> {

                HttpResponseMessage response =
                        objectMapper.readValue(
                                tunnelMessage.getPayload(),
                                HttpResponseMessage.class
                        );

                pendingRequestManager.complete(
                        tunnelMessage.getRequestId(),
                        response
                );

                log.info("HTTP Response received [{}]", tunnelMessage.getRequestId());
            }
            case CLIENT_REGISTER -> {

                ClientRegisterRequest request =
                        objectMapper.readValue(
                                tunnelMessage.getPayload(),
                                ClientRegisterRequest.class
                        );

                log.info("Client Registration Received");
                log.info("Machine : {}", request.getMachineName());
                log.info("OS      : {}", request.getOs());
                log.info("Version : {}", request.getVersion());

                String clientId = UUID.randomUUID().toString();
                clientManager.register(clientId, session);
                TunnelInfo tunnel = tunnelManager.createTunnel(clientId);

                String publicUrl =
                        "https://" + tunnel.getTunnelId() + ".tunnel.rajeshbandi.site";

                TunnelMessage response =
                        tunnelProtocolService.createClientRegisteredMessage(
                                clientId,
                                tunnel.getTunnelId(),
                                publicUrl
                        );

                session.sendMessage(
                        new TextMessage(
                                objectMapper.writeValueAsString(response)
                        )
                );

                log.info("Client [{}] registered successfully", clientId);
            }

            default -> {
                log.warn("Unsupported message type: {}", tunnelMessage.getType());
            }
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status)
            throws Exception {

        log.info("Client disconnected: {}", status);

        String clientId = clientManager.getClientId(session);

        if (clientId != null) {

            tunnelManager.removeTunnelByClientId(clientId);

            clientManager.unregister(session);
        }

        super.afterConnectionClosed(session, status);
    }
}
