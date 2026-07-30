package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.http.HttpResponseMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalHttpForwarder {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "host",
            "content-length",
            "transfer-encoding",
            "keep-alive",
            "proxy-connection",
            "te",
            "trailer",
            "upgrade"
    );

    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;
    private final HttpClient httpClient;

    public HttpResponseMessage forward(
            HttpRequestMessage request,
            String tunnelId
    ) throws IOException, InterruptedException {

        TunnelRuntime runtime =
                tunnelRuntimeRegistry.get(tunnelId);

        if (runtime == null) {
            throw new IllegalStateException(
                    "No runtime found for tunnel " + tunnelId
            );
        }

        String path = request.getPath();

        if (path == null || path.isBlank()) {
            path = "/";
        }

        StringBuilder targetUrl = new StringBuilder(
                "http://localhost:"
                        + runtime.getLocalPort()
                        + path
        );

        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            targetUrl.append("?").append(request.getQuery());
        }

        if (log.isDebugEnabled()) {
            log.debug("Forwarding Local HTTP Request: {} {} (Query: {}, Body: {}b)",
                    request.getMethod(), targetUrl, request.getQuery(),
                    request.getBody() == null ? 0 : request.getBody().length);
        } else {
            log.info("Forwarding {} {}", request.getMethod(), path);
        }

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl.toString()));

        // Forward all headers except hop-by-hop headers
        if (request.getHeaders() != null) {

            request.getHeaders().forEach((key, values) -> {

                if (log.isDebugEnabled()) {
                    log.debug("  Header: {} = {}", key, values);
                }

                if (HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
                    return;
                }

                for (String value : values) {
                    builder.header(key, value);
                }
            });
        }

        byte[] body =
                request.getBody() == null
                        ? new byte[0]
                        : request.getBody();

        String method = request.getMethod().toUpperCase();

        HttpRequest.BodyPublisher publisher =
                switch (method) {

                    case "GET", "HEAD", "OPTIONS" ->
                            HttpRequest.BodyPublishers.noBody();

                    default ->
                            HttpRequest.BodyPublishers.ofByteArray(body);

                };

        builder.method(method, publisher);

        HttpResponse<byte[]> response =
                httpClient.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        if (log.isDebugEnabled()) {
            log.debug("Local Response Status: {} (Body: {}b)", response.statusCode(), response.body().length);
        }

        return HttpResponseMessage.builder()
                .status(response.statusCode())
                .headers(new HashMap<>(response.headers().map()))
                .body(response.body())
                .build();
    }
}