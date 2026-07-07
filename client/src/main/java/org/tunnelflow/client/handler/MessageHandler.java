package org.tunnelflow.client.handler;


import org.tunnelflow.protocol.protocol.MessageType;
import org.tunnelflow.protocol.protocol.TunnelMessage;

public interface MessageHandler {
    MessageType getSupportedType();

    void handle(TunnelMessage message) throws Exception;
}
