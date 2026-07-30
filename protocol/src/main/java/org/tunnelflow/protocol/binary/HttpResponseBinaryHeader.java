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
public class HttpResponseBinaryHeader {
    private String requestId;
    private int status;
    private Map<String, List<String>> headers;
}
