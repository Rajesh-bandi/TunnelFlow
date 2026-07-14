package org.tunnelflow.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.service.LocalHttpForwarder;
import org.tunnelflow.client.service.TunnelProtocolService;
import org.tunnelflow.client.service.TunnelSender;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpRequestMessageHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final LocalHttpForwarder forwarder;
    private final TunnelProtocolService protocolService;
    private final TunnelSender tunnelSender;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public MessageType getSupportedType() {
        return MessageType.HTTP_REQUEST;
    }

    @Override
    public void handle(TunnelMessage message) {

        executor.submit(() -> {
            long start = System.nanoTime();
            try {

                HttpRequestMessage request =
                        objectMapper.readValue(
                                message.getPayload(),
                                HttpRequestMessage.class
                        );

                long beforeForward = System.nanoTime();

                HttpResponseMessage response =
                        forwarder.forward(
                                request,
                                message.getTunnelId()
                        );

                long afterForward = System.nanoTime();
                log.info(
                        "[{}] Local forwarding took {} ms",
                        message.getRequestId(),
                        (afterForward - beforeForward) / 1_000_000
                );
                TunnelMessage tunnelMessage =
                        protocolService.createHttpResponseMessage(
                                message.getRequestId(),
                                response
                        );

                tunnelSender.send(tunnelMessage);
                long end = System.nanoTime();

                log.info(
                        "[{}] Total client processing took {} ms",
                        message.getRequestId(),
                        (end - start) / 1_000_000
                );
            } catch (Exception e) {

                log.error(
                        "Failed to forward HTTP request [{}]",
                        message.getRequestId(),
                        e
                );

            }

        });

    }
}