package org.tunnelflow.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.RequestLog;
import org.tunnelflow.client.runtime.RequestLogRegistry;
import org.tunnelflow.client.service.LocalHttpForwarder;
import org.tunnelflow.client.service.TunnelProtocolService;
import org.tunnelflow.client.service.TunnelSender;
import org.tunnelflow.protocol.binary.BinaryMessageCodec;
import org.tunnelflow.protocol.binary.HttpRequestBinaryHeader;
import org.tunnelflow.protocol.binary.HttpResponseBinaryHeader;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpRequestMessageHandler implements MessageHandler {
    private final RequestLogRegistry requestLogRegistry;
    private final ObjectMapper objectMapper;
    private final LocalHttpForwarder forwarder;
    private final TunnelProtocolService protocolService;
    private final TunnelSender tunnelSender;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public MessageType getSupportedType() {
        return MessageType.HTTP_REQUEST;
    }

    public void handleBinary(HttpRequestBinaryHeader header, byte[] body) {
        executor.submit(() -> {
            long start = System.nanoTime();
            try {
                HttpRequestMessage request = new HttpRequestMessage(
                        header.getMethod(),
                        header.getPath(),
                        header.getHeaders(),
                        body,
                        header.getQuery()
                );

                long beforeForward = System.nanoTime();

                HttpResponseMessage response = forwarder.forward(
                        request,
                        header.getTunnelId()
                );

                long afterForward = System.nanoTime();
                long durationMs = (afterForward - beforeForward) / 1_000_000;

                RequestLog requestLog = RequestLog.builder()
                        .requestId(header.getRequestId())
                        .tunnelId(header.getTunnelId())
                        .method(request.getMethod())
                        .path(request.getPath())
                        .query(request.getQuery())
                        .status(response.getStatus())
                        .durationMs(durationMs)
                        .timestamp(Instant.now())
                        .requestHeaders(request.getHeaders())
                        .requestBody(request.getBody())
                        .responseHeaders(response.getHeaders())
                        .responseBody(response.getBody())
                        .build();

                requestLogRegistry.add(requestLog);

                HttpResponseBinaryHeader respHeader = HttpResponseBinaryHeader.builder()
                        .requestId(header.getRequestId())
                        .status(response.getStatus())
                        .headers(response.getHeaders())
                        .build();

                byte[] binaryFrame = BinaryMessageCodec.encode(respHeader, response.getBody());
                tunnelSender.sendBinary(binaryFrame);

                long end = System.nanoTime();
                log.debug("[{}] Binary request processed in {} ms", header.getRequestId(), (end - start) / 1_000_000);

            } catch (Exception e) {
                log.error("Failed to forward binary HTTP request [{}]", header.getRequestId(), e);

                try {
                    HttpResponseBinaryHeader errorHeader = HttpResponseBinaryHeader.builder()
                            .requestId(header.getRequestId())
                            .status(502)
                            .build();

                    byte[] errorBody = ("Local forwarding failed: " + e.getMessage()).getBytes();
                    byte[] errorFrame = BinaryMessageCodec.encode(errorHeader, errorBody);
                    tunnelSender.sendBinary(errorFrame);
                } catch (Exception ex) {
                    log.error("Failed to send binary error response for [{}]", header.getRequestId(), ex);
                }
            }
        });
    }

    @Override
    public void handle(TunnelMessage message) {
        executor.submit(() -> {
            long start = System.nanoTime();
            try {
                HttpRequestMessage request = objectMapper.readValue(
                        message.getPayload(),
                        HttpRequestMessage.class
                );

                long beforeForward = System.nanoTime();

                HttpResponseMessage response = forwarder.forward(
                        request,
                        message.getTunnelId()
                );

                long afterForward = System.nanoTime();
                long durationMs = (afterForward - beforeForward) / 1_000_000;

                RequestLog requestLog = RequestLog.builder()
                        .requestId(message.getRequestId())
                        .tunnelId(message.getTunnelId())
                        .method(request.getMethod())
                        .path(request.getPath())
                        .query(request.getQuery())
                        .status(response.getStatus())
                        .durationMs(durationMs)
                        .timestamp(Instant.now())
                        .requestHeaders(request.getHeaders())
                        .requestBody(request.getBody())
                        .responseHeaders(response.getHeaders())
                        .responseBody(response.getBody())
                        .build();

                requestLogRegistry.add(requestLog);

                TunnelMessage tunnelMessage = protocolService.createHttpResponseMessage(
                        message.getRequestId(),
                        response
                );

                tunnelSender.send(tunnelMessage);
                long end = System.nanoTime();

                log.debug("[{}] Total client processing took {} ms", message.getRequestId(), (end - start) / 1_000_000);
            } catch (Exception e) {
                log.error("Failed to forward HTTP request [{}]", message.getRequestId(), e);

                try {
                    HttpResponseMessage errorResponse = HttpResponseMessage.builder()
                            .status(502)
                            .body(("Local forwarding failed: " + e.getMessage()).getBytes())
                            .build();
                    TunnelMessage errorMsg = protocolService.createHttpResponseMessage(
                            message.getRequestId(), errorResponse);
                    tunnelSender.send(errorMsg);
                } catch (Exception ex) {
                    log.error("Failed to send error response for [{}]", message.getRequestId(), ex);
                }
            }
        });
    }
}