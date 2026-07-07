package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.handler.MessageHandler;
import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TunnelMessageDispatcher {

    Map<MessageType,MessageHandler> handlers;
    public TunnelMessageDispatcher(List<MessageHandler> messageHandlers) {
        this.handlers = messageHandlers.stream()
                .collect(Collectors.toMap(
                        MessageHandler::getSupportedType,
                        Function.identity()
                ));
    }
    public void dispatch(TunnelMessage message) {
        MessageHandler handler = handlers.get(message.getType());

        if(handler == null){
            log.error("No handler for message type {}", message.getType());
        }

        try {
            handler.handle(message);
        } catch (Exception e) {
            log.error("Failed to dispatch message", e);
        }
    }
}
