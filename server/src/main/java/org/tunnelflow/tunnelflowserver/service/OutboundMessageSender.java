package org.tunnelflow.tunnelflowserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundMessageSender {

    private final ObjectMapper objectMapper;

    public Thread start(ClientConnection connection) {
        Thread senderThread = Thread.startVirtualThread(() -> {
            while (connection.getSession().isOpen()) {
                try {
                    TunnelMessage message =
                            connection.getOutboundQueue().poll(5, TimeUnit.SECONDS);
                    if (message == null) continue;
                    String json =
                            objectMapper.writeValueAsString(message);
                    connection.getSession().sendMessage(
                            new TextMessage(json)
                    );
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (Exception e) {
                    log.error("Failed sending websocket message", e);
                    break;
                }
            }
            log.info("Outbound sender thread exiting for session [{}]",
                    connection.getSession().getId());
        });
        return senderThread;
    }
}