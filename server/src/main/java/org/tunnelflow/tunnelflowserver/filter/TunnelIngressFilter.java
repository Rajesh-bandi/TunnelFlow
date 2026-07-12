package org.tunnelflow.tunnelflowserver.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.tunnelflowserver.model.TunnelInfo;
import org.tunnelflow.tunnelflowserver.service.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TunnelIngressFilter extends OncePerRequestFilter {

    private final TunnelProtocolService tunnelProtocolService;
    private final HttpRequestMapper httpRequestMapper;
    private final PendingRequestManager pendingRequestManager;
    private final ClientManager clientManager;
    private final TunnelManager tunnelManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        byte[] body = request.getInputStream().readAllBytes();

        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String host = request.getServerName();

        if (!host.endsWith(".tunnel.rajeshbandi.site")) {
            filterChain.doFilter(request, response);
            return;
        }

        String tunnelId = host.substring(
                0,
                host.indexOf(".tunnel.rajeshbandi.site")
        );

        log.info("========================================================");
        log.info("Incoming Tunnel Request");
        log.info("Host      : {}", host);
        log.info("Tunnel ID : {}", tunnelId);
        log.info("Method    : {}", request.getMethod());
        log.info("Path      : {}", request.getRequestURI());
        log.info("Query     : {}", request.getQueryString());
        log.info("Body Size : {}", body.length);

        log.info("Request Headers:");
        Collections.list(request.getHeaderNames()).forEach(name ->
                log.info("{} = {}", name, Collections.list(request.getHeaders(name)))
        );

        TunnelInfo tunnel = tunnelManager.getTunnel(tunnelId);

        if (tunnel == null) {
            log.error("Tunnel [{}] not found", tunnelId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tunnel not found.");
            return;
        }

        WebSocketSession session = clientManager.getSession(tunnel.getClientId());

        if (session == null || !session.isOpen()) {
            log.error("Client [{}] is offline", tunnel.getClientId());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Tunnel client is offline.");
            return;
        }

        String requestId = UUID.randomUUID().toString();

        CompletableFuture<HttpResponseMessage> future =
                pendingRequestManager.register(requestId);

        TunnelMessage message =
                tunnelProtocolService.createHttpRequestTunnelMessage(
                        httpRequestMapper.map(request, body),
                        requestId
                );

        log.info("Sending HTTP_REQUEST [{}] to client [{}]",
                requestId,
                tunnel.getClientId());

        session.sendMessage(
                new TextMessage(
                        objectMapper.writeValueAsString(message)
                )
        );

        HttpResponseMessage tunnelResponse;

        try {
            tunnelResponse = future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Tunnel request [{}] timed out", requestId);
            response.sendError(
                    HttpServletResponse.SC_GATEWAY_TIMEOUT,
                    "Tunnel request timed out."
            );
            return;
        } catch (Exception e) {
            log.error("Tunnel request [{}] failed", requestId, e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Tunnel failed."
            );
            return;
        }

        log.info("HTTP_RESPONSE [{}] received", requestId);
        log.info("Status      : {}", tunnelResponse.getStatus());
        log.info("Body Length : {}",
                tunnelResponse.getBody() == null ? 0 : tunnelResponse.getBody().length);

        log.info("Response Headers:");
        if (tunnelResponse.getHeaders() != null) {
            tunnelResponse.getHeaders().forEach((k, v) ->
                    log.info("{} = {}", k, v));
        }

        response.setStatus(tunnelResponse.getStatus());

        if (tunnelResponse.getHeaders() != null) {

            tunnelResponse.getHeaders().forEach((name, values) -> {

                if (name.equalsIgnoreCase("Transfer-Encoding")
                        || name.equalsIgnoreCase("Connection")
                        || name.equalsIgnoreCase("Keep-Alive")
                        || name.equalsIgnoreCase("Upgrade")
                        || name.equalsIgnoreCase("Proxy-Authenticate")
                        || name.equalsIgnoreCase("Proxy-Authorization")
                        || name.equalsIgnoreCase("TE")
                        || name.equalsIgnoreCase("Trailer")) {
                    log.info("Skipping hop-by-hop header: {}", name);
                    return;
                }

                log.info("Copying Header: {} -> {}", name, values);

                for (String value : values) {
                    response.addHeader(name, value);
                }

            });
        }

        if (tunnelResponse.getBody() != null) {
            log.info("Writing {} bytes to response",
                    tunnelResponse.getBody().length);
            response.getOutputStream().write(tunnelResponse.getBody());
        }

        response.getOutputStream().flush();

        log.info("Tunnel request [{}] completed successfully", requestId);
        log.info("========================================================");
    }
}