package org.tunnelflow.tunnelflowserver.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
@RequiredArgsConstructor
public class ClientConnection {

    private final WebSocketSession session;

    private final BlockingQueue<WebSocketMessage<?>> outboundQueue =
            new LinkedBlockingQueue<>(200); // bounded — prevents OOM when client is slow

    @Setter
    private volatile Thread senderThread;

    public void shutdown() {
        if (senderThread != null) {
            senderThread.interrupt();
        }
        outboundQueue.clear();
    }
}