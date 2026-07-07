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
        TunnelMessage message = null;
        try {
            message = objectMapper.readValue(json, TunnelMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize TunnelMessage.", e);
        }
        dispatcher.dispatch(message);
    }
}
