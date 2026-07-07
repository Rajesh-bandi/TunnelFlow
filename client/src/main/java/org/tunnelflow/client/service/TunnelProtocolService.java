package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpResponseMessage;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;


@Service
public class TunnelProtocolService {

    private final ObjectMapper objectMapper;

    public TunnelProtocolService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TunnelMessage createPongMessage(String requestId) {
        return TunnelMessage.builder()
                .requestId(requestId)
                .type(MessageType.PONG)
                .payload("PONG")
                .build();
    }

    public TunnelMessage createHttpResponseMessage(
            String requestId,
            HttpResponseMessage response
    ) throws JsonProcessingException {

        return TunnelMessage.builder()
                .requestId(requestId)
                .type(MessageType.HTTP_RESPONSE)
                .payload(objectMapper.writeValueAsString(response))
                .build();
    }

}
