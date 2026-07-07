package org.tunnelflow.tunnelflowserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;


import java.util.UUID;

@Service
public class TunnelProtocolService {

    private final ObjectMapper objectMapper;

    public TunnelProtocolService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TunnelMessage createPingMessage() {
        return TunnelMessage.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.PING)
                .payload("PING")
                .build();
    }

    public TunnelMessage createPongMessage(String requestId) {
        return TunnelMessage.builder()
                .id(requestId)
                .type(MessageType.PONG)
                .payload("PONG")
                .build();
    }

    public TunnelMessage createHttpRequestTunnelMessage(HttpRequestMessage request, String requestId)
            throws JsonProcessingException {

        return TunnelMessage.builder()
                .id(requestId)
                .type(MessageType.HTTP_REQUEST)
                .payload(objectMapper.writeValueAsString(request))
                .build();
    }
}
