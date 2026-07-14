package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

@Service
@RequiredArgsConstructor
public class ClientTunnelManager {
    private final TunnelConnectionManager tunnelConnectionManager;

    private final TunnelCreationService tunnelCreationService;

    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;
    public TunnelRuntime expose(int port) throws JsonProcessingException {
        tunnelConnectionManager.connect();
        TunnelCreatedResponse response =
                tunnelCreationService.createTunnel(port);

        TunnelRuntime runtime =
                TunnelRuntime.builder()
                        .tunnelId(response.getTunnelId())
                        .localPort(port)
                        .publicUrl(response.getPublicUrl())
                        .build();

        tunnelRuntimeRegistry.register(runtime);

        return runtime;

    }

}