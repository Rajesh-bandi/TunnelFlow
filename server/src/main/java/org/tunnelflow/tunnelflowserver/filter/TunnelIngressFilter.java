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
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.tunnelflowserver.model.TunnelInfo;
import org.tunnelflow.tunnelflowserver.service.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TunnelIngressFilter extends OncePerRequestFilter {

    private final TunnelProtocolService tunnelProtocolService;
    private final HttpRequestMapper httpRequestMapper;
    private final PendingRequestManager pendingRequestManager;
    private final ClientManager clientManager;
    private final TunnelManager tunnelManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // --- Early exits BEFORE reading the body (saves memory for non-tunnel traffic) ---

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

        // --- Only read the body once we know this is a real tunnel request ---
        byte[] body = request.getInputStream().readAllBytes();

        String tunnelId = host.substring(
                0,
                host.indexOf(".tunnel.rajeshbandi.site")
        );

        log.info("Incoming tunnel request | tunnelId={} method={} path={} bodyBytes={}",
                tunnelId, request.getMethod(), request.getRequestURI(), body.length);

        // Debug-only: full headers (silent in production)
        if (log.isDebugEnabled()) {
            log.debug("Request headers for [{}]:", tunnelId);
            java.util.Collections.list(request.getHeaderNames()).forEach(name ->
                    log.debug("  {} = {}", name, java.util.Collections.list(request.getHeaders(name)))
            );
        }

        TunnelInfo tunnel = tunnelManager.getTunnel(tunnelId);

        if (tunnel == null) {
            log.warn("Tunnel [{}] not found", tunnelId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tunnel not found.");
            return;
        }

        ClientConnection connection =
                clientManager.getConnection(tunnel.getClientId());

        if (connection == null
                || connection.getSession() == null
                || !connection.getSession().isOpen()) {

            log.warn("Client [{}] is offline for tunnel [{}]", tunnel.getClientId(), tunnelId);

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Tunnel client is offline."
            );

            return;
        }

        String requestId = UUID.randomUUID().toString();

        CompletableFuture<HttpResponseMessage> future =
                pendingRequestManager.register(requestId);

        TunnelMessage message =
                tunnelProtocolService.createHttpRequestTunnelMessage(
                        httpRequestMapper.map(request, body),
                        requestId,
                        tunnel.getTunnelId()
                );

        log.debug("Queued HTTP_REQUEST [{}] for client [{}]", requestId, tunnel.getClientId());

        boolean queued = connection.getOutboundQueue().offer(message);
        if (!queued) {
            // Queue full — cancel the future immediately to avoid a leak
            pendingRequestManager.cancel(requestId);
            log.error("Outbound queue full for client [{}], dropping request [{}]",
                    tunnel.getClientId(), requestId);
            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Client is busy. Please retry."
            );
            return;
        }

        HttpResponseMessage tunnelResponse;

        try {
            tunnelResponse = future.get(30, TimeUnit.SECONDS);
            log.info("Tunnel response received | requestId={} status={} bodyBytes={}",
                    requestId,
                    tunnelResponse.getStatus(),
                    tunnelResponse.getBody() == null ? 0 : tunnelResponse.getBody().length);

        } catch (TimeoutException e) {
            // Cancel the future to remove it from the map — prevents leak
            pendingRequestManager.cancel(requestId);
            log.error("Tunnel request [{}] timed out", requestId);
            response.sendError(
                    HttpServletResponse.SC_GATEWAY_TIMEOUT,
                    "Tunnel request timed out."
            );
            return;

        } catch (Exception e) {
            pendingRequestManager.cancel(requestId);
            log.error("Tunnel request [{}] failed", requestId, e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
            return;
        }

        response.setStatus(tunnelResponse.getStatus());

        if (tunnelResponse.getHeaders() != null) {

            tunnelResponse.getHeaders().forEach((name, values) -> {

                // Skip hop-by-hop headers — they must not be forwarded by proxies
                if (name.equalsIgnoreCase("Transfer-Encoding")
                        || name.equalsIgnoreCase("Connection")
                        || name.equalsIgnoreCase("Keep-Alive")
                        || name.equalsIgnoreCase("Upgrade")
                        || name.equalsIgnoreCase("Proxy-Authenticate")
                        || name.equalsIgnoreCase("Proxy-Authorization")
                        || name.equalsIgnoreCase("TE")
                        || name.equalsIgnoreCase("Trailer")) {
                    log.debug("Skipping hop-by-hop header: {}", name);
                    return;
                }

                for (String value : values) {
                    response.addHeader(name, value);
                }

            });
        }

        if (tunnelResponse.getBody() != null && tunnelResponse.getBody().length > 0) {
            response.getOutputStream().write(tunnelResponse.getBody());
        }

        response.getOutputStream().flush();

        log.debug("Tunnel request [{}] completed successfully", requestId);
    }
}