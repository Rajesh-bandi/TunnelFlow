package org.tunnelflow.client.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.websocket.TunnelWebSocketClient;


@Service
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class TunnelConnectionManager {
    private final TunnelWebSocketClient client;
    public void  connect(){
        client.connect();
    }
}
