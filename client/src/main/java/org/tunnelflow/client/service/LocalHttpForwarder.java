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

        if (path.isBlank()) {
            path = "/";
        }

        String targetUrl = baseUrl + path;

        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            targetUrl += "?" + request.getQuery();
        }

        log.info("==================================================");
        log.info("Forwarding Local HTTP Request");
        log.info("Method      : {}", request.getMethod());
        log.info("Target URL  : {}", targetUrl);
        log.info("Query       : {}", request.getQuery());
        log.info("Body Length : {}",
                request.getBody() == null ? 0 : request.getBody().length);

        if (request.getHeaders() != null) {
            log.info("Incoming Headers:");
            request.getHeaders().forEach((k, v) ->
                    log.info("{} = {}", k, v));
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl));

        // Keep this commented for now.
//    if (request.getHeaders() != null) {
//        request.getHeaders().forEach((key, values) -> {
//            if (HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
//                return;
//            }
//            for (String value : values) {
//                builder.header(key, value);
//            }
//        });
//    }

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

        log.info("--------------- Local Response ----------------");
        log.info("Status      : {}", response.statusCode());

        response.headers().map().forEach((k, v) ->
                log.info("{} = {}", k, v));

        log.info("Body Length : {}", response.body().length);
        log.info("==================================================");

        return HttpResponseMessage.builder()
                .status(response.statusCode())
                .headers(new HashMap<>(response.headers().map()))
                .body(response.body())
                .build();
    }
}