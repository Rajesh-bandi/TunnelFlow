package org.tunnelflow.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.client.service.ClientTunnelManager;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/tunnels")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TunnelController {

    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;
    private final ClientTunnelManager clientTunnelManager;

    @PostMapping
    public Map<String, Object> createTunnel(
            @RequestBody Map<String, Integer> request
    ) throws JsonProcessingException {

        Integer port = request.get("port");

        if (port == null || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        TunnelRuntime runtime =
                clientTunnelManager.expose(port);

        return Map.of(
                "tunnelId", runtime.getTunnelId(),
                "localPort", runtime.getLocalPort(),
                "publicUrl", runtime.getPublicUrl()
        );
    }
    @GetMapping
    public Collection<TunnelRuntime> getActiveTunnels() {
        return tunnelRuntimeRegistry.getAll();
    }
}