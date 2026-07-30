package org.tunnelflow.protocol.binary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpRequestBinaryHeader {
    private String requestId;
    private String tunnelId;
    private String method;
    private String path;
    private String query;
    private Map<String, List<String>> headers;
}
