package org.tunnelflow.tunnelflowserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundMessageSender {

    private final ObjectMapper objectMapper;

    public void start(ClientConnection connection) {

        Thread.startVirtualThread(() -> {

            while (true) {

                try {

                    if (!connection.getSession().isOpen()) {
                        break;
                    }

                    TunnelMessage message =
                            connection.getOutboundQueue().take();

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

        });

    }

}