package org.tunnelflow.tunnelflowserver.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.tunnelflow.protocol.protocol.TunnelMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
@RequiredArgsConstructor
public class ClientConnection {

    private final WebSocketSession session;

    private final BlockingQueue<TunnelMessage> outboundQueue =
            new LinkedBlockingQueue<>();

}