package org.tunnelflow.tunnelflowserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.http.HttpRequestMessage;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;
import org.tunnelflow.protocol.protocol.client.ClientRegisteredResponse;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

import java.util.UUID;

@Service
public class TunnelProtocolService {

    private final ObjectMapper objectMapper;

    public TunnelProtocolService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TunnelMessage createPingMessage() {
        return TunnelMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .type(MessageType.PING)
                .payload("PING")
                .build();
    }
    public TunnelMessage createTunnelCreatedMessage(
            String tunnelId,
            String publicUrl
    ) throws JsonProcessingException {

        TunnelCreatedResponse response =
                TunnelCreatedResponse.builder()
                        .tunnelId(tunnelId)
                        .publicUrl(publicUrl)
                        .build();

        return TunnelMessage.builder()
                .type(MessageType.TUNNEL_CREATED)
                .payload(objectMapper.writeValueAsString(response))
                .build();
    }

    public TunnelMessage createPongMessage(String requestId) {
        return TunnelMessage.builder()
                .requestId(requestId)
                .type(MessageType.PONG)
                .payload("PONG")
                .build();
    }

    public TunnelMessage createHttpRequestTunnelMessage(HttpRequestMessage request, String requestId)
            throws JsonProcessingException {

        return TunnelMessage.builder()
                .requestId(requestId)
                .type(MessageType.HTTP_REQUEST)
                .payload(objectMapper.writeValueAsString(request))
                .build();
    }

    public TunnelMessage createClientRegisteredMessage(String clientId)
            throws JsonProcessingException {

        ClientRegisteredResponse response =
                ClientRegisteredResponse.builder()
                        .clientId(clientId)
                        .build();

        return TunnelMessage.builder()
                .type(MessageType.CLIENT_REGISTERED)
                .payload(objectMapper.writeValueAsString(response))
                .build();
    }

}
