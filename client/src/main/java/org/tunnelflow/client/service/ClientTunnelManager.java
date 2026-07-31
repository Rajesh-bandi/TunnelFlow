package org.tunnelflow.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientTunnelManager {

    private final TunnelConnectionManager tunnelConnectionManager;
    private final TunnelDeletionService tunnelDeletionService;
    private final TunnelCreationService tunnelCreationService;
    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;

    public TunnelRuntime expose(int port) throws JsonProcessingException {
        tunnelConnectionManager.connect();
        TunnelCreatedResponse response = tunnelCreationService.createTunnel(port);

        TunnelRuntime runtime = TunnelRuntime.builder()
                .tunnelId(response.getTunnelId())
                .localPort(port)
                .publicUrl(response.getPublicUrl())
                .build();

        tunnelRuntimeRegistry.register(runtime);

        return runtime;
    }

    public void deleteTunnel(String tunnelId) throws JsonProcessingException {
        TunnelRuntime runtime = tunnelRuntimeRegistry.get(tunnelId);

        if (runtime == null) {
            log.info("Tunnel [{}] already deleted or removed from registry", tunnelId);
            return;
        }

        try {
            tunnelDeletionService.deleteTunnel(tunnelId);
        } catch (Exception e) {
            log.warn("Failed to complete remote tunnel deletion for [{}]: {}", tunnelId, e.getMessage());
        } finally {
            tunnelRuntimeRegistry.remove(tunnelId);
        }
    }
}