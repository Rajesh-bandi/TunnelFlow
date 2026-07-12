package org.tunnelflow.tunnelflowserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.tunnelflowserver.model.TunnelInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TunnelManager {

    private final Map<String, TunnelInfo> tunnels =
            new ConcurrentHashMap<>();

    public void register(TunnelInfo tunnelInfo) {

        tunnels.put(
                tunnelInfo.getTunnelId(),
                tunnelInfo
        );

        log.info("Tunnel [{}] registered for client [{}]",
                tunnelInfo.getTunnelId(),
                tunnelInfo.getClientId());
    }

    public TunnelInfo getTunnel(String tunnelId) {

        return tunnels.get(tunnelId);
    }

    public void removeTunnel(String tunnelId) {

        tunnels.remove(tunnelId);

        log.info("Tunnel [{}] removed", tunnelId);
    }

    public boolean contains(String tunnelId) {

        return tunnels.containsKey(tunnelId);
    }

    public int getTunnelCount() {

        return tunnels.size();
    }
}