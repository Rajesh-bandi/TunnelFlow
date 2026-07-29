package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.protocol.protocol.TunnelMessage;


@Service
@Slf4j
@RequiredArgsConstructor
public class TunnelMessageReceiver {
    private final ObjectMapper objectMapper;
    private final TunnelMessageDispatcher dispatcher;
    public void receive(String json) {
        try {
            TunnelMessage message = objectMapper.readValue(json, TunnelMessage.class);
            dispatcher.dispatch(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize TunnelMessage.", e);
        }
    }
}
