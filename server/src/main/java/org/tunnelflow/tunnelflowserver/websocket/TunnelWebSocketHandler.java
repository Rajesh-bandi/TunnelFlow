package org.tunnelflow.tunnelflowserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.tunnelflowserver.service.PendingRequestManager;
import org.tunnelflow.tunnelflowserver.service.TunnelProtocolService;
import org.tunnelflow.tunnelflowserver.service.TunnelSessionManager;

import static org.tunnelflow.protocol.protocol.MessageType.HTTP_RESPONSE;

@Slf4j
@Component
@RequiredArgsConstructor
public class TunnelWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final TunnelSessionManager sessionManager;
    private final TunnelProtocolService tunnelProtocolService;
    private final PendingRequestManager pendingRequestManager;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.register(session);
        TunnelMessage tunnelMessage = tunnelProtocolService.createPingMessage();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(tunnelMessage)));
        log.info("Client connected");
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

            default -> {
                log.warn("Unsupported message type: {}", tunnelMessage.getType());
            }
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      org.springframework.web.socket.CloseStatus status)
            throws Exception {

        log.info("Client disconnected: {}", status);

        sessionManager.unRegister(session);

        super.afterConnectionClosed(session, status);
    }
}
