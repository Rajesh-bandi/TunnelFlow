package org.tunnelflow.tunnelflowserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.tunnelflowserver.service.HttpRequestMapper;
import org.tunnelflow.tunnelflowserver.service.PendingRequestManager;
import org.tunnelflow.tunnelflowserver.service.TunnelProtocolService;
import org.tunnelflow.tunnelflowserver.service.TunnelSessionManager;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/tunnel/**")
public class TunnelIngressController {
    private final TunnelSessionManager tunnelSessionManager;
    private final TunnelProtocolService tunnelProtocolService;
    private final HttpRequestMapper httpRequestMapper;
    private final PendingRequestManager pendingRequestManager;
    @RequestMapping
    public CompletableFuture<ResponseEntity<?>> tunnelIngress(@RequestBody(required = false)byte[] body, HttpServletRequest request) throws Exception{
        String requestId = UUID.randomUUID().toString();
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("Body: {}", body);
        CompletableFuture<HttpResponseMessage> future =
                pendingRequestManager.register(requestId);
        TunnelMessage message = tunnelProtocolService.createHttpRequestTunnelMessage(httpRequestMapper.map(request,body),requestId);
        tunnelSessionManager.send(message);
        return future.thenApply(response -> {
            log.info("Response status: {}", response.getStatus());
            log.info("Response Content-Type: {}", response.getHeaders());
            log.info("Body length: {}", response.getBody() == null ? "null" : response.getBody().length);
            log.info("Body first bytes: {}", response.getBody() == null ? "null" : new String(response.getBody(), 0, Math.min(50, response.getBody().length)));
            HttpHeaders headers = new HttpHeaders();

            if (response.getHeaders() != null) {
                headers.putAll(response.getHeaders());
            }

            return ResponseEntity
                    .status(response.getStatus())
                    .headers(headers)
                    .body(response.getBody());
        });
    }

}

