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
    public TunnelInfo createTunnel(String clientId,int localPort) {

        String tunnelId = generateTunnelId();

        TunnelInfo tunnelInfo = TunnelInfo.builder()
                .tunnelId(tunnelId)
                .clientId(clientId)
                .localPort(localPort)
                .build();

        tunnels.put(tunnelId, tunnelInfo);

        log.info("Tunnel [{}] created for client [{}]",
                tunnelId,
                clientId);

        return tunnelInfo;
    }
    private String generateTunnelId() {

        return java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

    }
    public void removeTunnelByClientId(String clientId) {

        tunnels.entrySet().removeIf(entry -> {

            boolean remove =
                    entry.getValue().getClientId().equals(clientId);

            if (remove) {
                log.info("Tunnel [{}] removed for client [{}]",
                        entry.getKey(),
                        clientId);
            }

            return remove;
        });
    }
}