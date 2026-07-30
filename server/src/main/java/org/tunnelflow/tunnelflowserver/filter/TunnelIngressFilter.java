package org.tunnelflow.tunnelflowserver.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.BinaryMessage;
import org.tunnelflow.protocol.binary.BinaryMessageCodec;
import org.tunnelflow.protocol.binary.HttpRequestBinaryHeader;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.tunnelflowserver.model.TunnelInfo;
import org.tunnelflow.tunnelflowserver.service.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TunnelIngressFilter extends OncePerRequestFilter {

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

        // Allow WebSocket requests
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow Spring error handling
        if (path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String host = request.getServerName();

        // Only intercept tunnel requests
        if (!host.endsWith(".tunnelflow.rajeshbandi.site")) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] body = request.getInputStream().readAllBytes();

        String tunnelId = host.substring(
                0,
                host.indexOf(".tunnelflow.rajeshbandi.site")
        );

        log.debug(
                "Incoming tunnel request | tunnelId={} method={} path={} bodyBytes={}",
                tunnelId,
                request.getMethod(),
                request.getRequestURI(),
                body.length
        );

        TunnelInfo tunnel = tunnelManager.getTunnel(tunnelId);

        if (tunnel == null) {
            log.warn("Tunnel [{}] not found", tunnelId);
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Tunnel not found."
            );
            return;
        }

        ClientConnection connection =
                clientManager.getConnection(tunnel.getClientId());

        if (connection == null
                || connection.getSession() == null
                || !connection.getSession().isOpen()) {

            log.warn(
                    "Client [{}] is offline for tunnel [{}]",
                    tunnel.getClientId(),
                    tunnelId
            );

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Tunnel client is offline."
            );

            return;
        }

        String requestId = UUID.randomUUID().toString();

        CompletableFuture<HttpResponseMessage> future =
                pendingRequestManager.register(requestId);

        Map<String, List<String>> headersMap = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(name ->
                headersMap.put(name, Collections.list(request.getHeaders(name)))
        );

        HttpRequestBinaryHeader binaryHeader = HttpRequestBinaryHeader.builder()
                .requestId(requestId)
                .tunnelId(tunnel.getTunnelId())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .query(request.getQueryString())
                .headers(headersMap)
                .build();

        BinaryMessage binaryMessage;
        try {
            byte[] binaryFrame = BinaryMessageCodec.encode(binaryHeader, body);
            binaryMessage = new BinaryMessage(binaryFrame);
        } catch (Exception e) {
            pendingRequestManager.cancel(requestId);
            log.error("Failed to encode binary request for [{}]", requestId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Encoding error.");
            return;
        }

        boolean queued = connection.getOutboundQueue().offer(binaryMessage);

        if (!queued) {

            pendingRequestManager.cancel(requestId);

            log.error(
                    "Outbound queue full for client [{}], dropping request [{}]",
                    tunnel.getClientId(),
                    requestId
            );

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Client is busy. Please retry."
            );

            return;
        }

        HttpResponseMessage tunnelResponse;

        try {

            tunnelResponse = future.get(30, TimeUnit.SECONDS);

            log.debug(
                    "Tunnel response received | requestId={} status={} bodyBytes={}",
                    requestId,
                    tunnelResponse.getStatus(),
                    tunnelResponse.getBody() == null
                            ? 0
                            : tunnelResponse.getBody().length
            );

        } catch (TimeoutException e) {

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

        if (tunnelResponse.getBody() != null
                && tunnelResponse.getBody().length > 0) {

            response.getOutputStream().write(tunnelResponse.getBody());

        }

        response.getOutputStream().flush();

        log.debug("Tunnel request [{}] completed successfully", requestId);
    }
}