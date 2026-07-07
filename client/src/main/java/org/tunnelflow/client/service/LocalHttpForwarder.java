package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.http.HttpResponseMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${tunnelflow.local.base-url}")
    private String baseUrl;

    private final HttpClient httpClient;

    public HttpResponseMessage forward(HttpRequestMessage request)
            throws IOException, InterruptedException {

        String path = request.getPath();
        log.info("HttpClient class: {}", httpClient.getClass().getName());
        // Remove tunnel prefix
        if (path.startsWith("/tunnel")) {
            path = path.substring("/tunnel".length());
        }

        if (path.isBlank()) {
            path = "/";
        }

        // Build target URL
        String targetUrl = baseUrl + path;

        // Append query parameters
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            targetUrl += "?" + request.getQuery();
        }

        log.info("Forwarding request to {}", targetUrl);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl));

        // Copy request headers
//        if (request.getHeaders() != null) {
//
//            request.getHeaders().forEach((key, values) -> {
//
//                if (HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
//                    return;
//                }
//
//                for (String value : values) {
//                    builder.header(key, value);
//                }
//
//            });
//        }

        byte[] body = request.getBody() == null
                ? new byte[0]
                : request.getBody();

        switch (request.getMethod().toUpperCase()) {

            case "GET" -> builder.GET();

            case "POST" ->
                    builder.POST(HttpRequest.BodyPublishers.ofByteArray(body));

            case "PUT" ->
                    builder.PUT(HttpRequest.BodyPublishers.ofByteArray(body));

            case "DELETE" ->
                    builder.method(
                            "DELETE",
                            HttpRequest.BodyPublishers.ofByteArray(body)
                    );

            case "PATCH" ->
                    builder.method(
                            "PATCH",
                            HttpRequest.BodyPublishers.ofByteArray(body)
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported HTTP method: " + request.getMethod()
                    );
        }

        HttpResponse<byte[]> response =
                httpClient.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        Map<String, List<String>> headers =
                new HashMap<>(response.headers().map());

        return HttpResponseMessage.builder()
                .status(response.statusCode())
                .headers(headers)
                .body(response.body())
                .build();
    }
}