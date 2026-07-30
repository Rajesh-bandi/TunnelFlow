package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.handler.HttpRequestMessageHandler;
import org.tunnelflow.protocol.binary.BinaryMessageCodec;
import org.tunnelflow.protocol.binary.HttpRequestBinaryHeader;
import org.tunnelflow.protocol.protocol.TunnelMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class TunnelMessageReceiver {
    private final ObjectMapper objectMapper;
    private final TunnelMessageDispatcher dispatcher;
    private final HttpRequestMessageHandler httpRequestHandler;

    public void receive(String json) {
        try {
            TunnelMessage message = objectMapper.readValue(json, TunnelMessage.class);
            dispatcher.dispatch(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize TunnelMessage.", e);
        }
    }

    public void receiveBinary(byte[] bytes) {
        try {
            BinaryMessageCodec.BinaryDecodedMessage<HttpRequestBinaryHeader> decoded =
                    BinaryMessageCodec.decode(bytes, HttpRequestBinaryHeader.class);

            httpRequestHandler.handleBinary(decoded.getHeader(), decoded.getBody());
        } catch (Exception e) {
            log.error("Failed to decode binary message on client", e);
        }
    }
}
