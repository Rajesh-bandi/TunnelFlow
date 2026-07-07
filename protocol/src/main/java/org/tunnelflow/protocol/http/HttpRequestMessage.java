package org.tunnelflow.protocol.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpRequestMessage {
    private String method;

    private String path;

    private Map<String, List<String>> headers;

    private byte[] body;

    private String query;
}

