package org.tunnelflow.client.runtime;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TunnelRuntimeRegistry {

    private final Map<String, TunnelRuntime> tunnels =
            new ConcurrentHashMap<>();

    public void register(TunnelRuntime runtime) {

        tunnels.put(
                runtime.getTunnelId(),
                runtime
        );

    }

    public TunnelRuntime get(String tunnelId) {

        return tunnels.get(tunnelId);

    }

    public void remove(String tunnelId) {

        tunnels.remove(tunnelId);

    }

    public boolean contains(String tunnelId) {

        return tunnels.containsKey(tunnelId);

    }

}