package org.tunnelflow.tunnelflowserver.handler;


import org.tunnelflow.protocol.protocol.TunnelMessage;

public interface MessageHandler {
    public void handleMessage(TunnelMessage message);
    public boolean canHandle(TunnelMessage message);
}
