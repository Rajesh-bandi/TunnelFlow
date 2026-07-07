package org.tunnelflow.tunnelflowserver.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpRequestMessage;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpRequestMapper {

    public HttpRequestMessage map(HttpServletRequest request, byte[] body) {
        HttpRequestMessage httpRequestMessage = new HttpRequestMessage();
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(
                    headerName,
                    Collections.list(request.getHeaders(headerName))
            );
        }
        httpRequestMessage.setHeaders(headers);
        httpRequestMessage.setBody(body);
        httpRequestMessage.setPath(request.getRequestURI());
        httpRequestMessage.setQuery(request.getQueryString());
        httpRequestMessage.setMethod(request.getMethod());

        return httpRequestMessage;
    }
}