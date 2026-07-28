package org.tunnelflow.client.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    private String requestId;
    private String tunnelId;

    private String method;
    private String path;
    private String query;

    private int status;

    private long durationMs;

    private Instant timestamp;

    private Map<String, List<String>> requestHeaders;
    private byte[] requestBody;

    private Map<String, List<String>> responseHeaders;
    private byte[] responseBody;
}