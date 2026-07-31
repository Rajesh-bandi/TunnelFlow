package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.runtime.ApplicationRuntimeRegistry;
import org.tunnelflow.client.runtime.TunnelRuntime;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceholderResolver {

    private final ApplicationRuntimeRegistry applicationRuntimeRegistry;

    public void resolve(Map<String, ApplicationDeployment> deployments) {
        // Collect all available tunnels from active runtimes and current batch
        Map<String, TunnelRuntime> availableTunnels = new HashMap<>();

        // 1. Load tunnels from existing active applications in registry
        applicationRuntimeRegistry.getAll().forEach(appRuntime -> {
            if (appRuntime.getConfig() != null && appRuntime.getTunnelRuntime() != null) {
                availableTunnels.put(appRuntime.getConfig().getName(), appRuntime.getTunnelRuntime());
            }
        });

        // 2. Load tunnels from current deployment batch (overriding if needed)
        deployments.forEach((name, deployment) -> {
            if (deployment.getTunnelRuntime() != null) {
                availableTunnels.put(name, deployment.getTunnelRuntime());
            }
        });

        // 3. Perform placeholder resolution for all deployments
        for (ApplicationDeployment deployment : deployments.values()) {

            Map<String, String> configured = deployment.getConfig().getEnv();
            if (configured != null) {
                deployment.getResolvedEnvironment().putAll(configured);
            }

            Map<String, String> environment = deployment.getResolvedEnvironment();

            for (Map.Entry<String, String> entry : environment.entrySet()) {
                String value = entry.getValue();
                if (value == null || !value.contains("${")) {
                    continue;
                }

                String resolvedValue = value;

                for (Map.Entry<String, TunnelRuntime> target : availableTunnels.entrySet()) {
                    String appName = target.getKey();
                    TunnelRuntime tunnelRuntime = target.getValue();

                    if (tunnelRuntime != null) {
                        String pubUrl = tunnelRuntime.getPublicUrl();
                        String locUrl = "http://localhost:" + tunnelRuntime.getLocalPort();

                        resolvedValue = resolvedValue
                                .replace("${" + appName + ".publicUrl}", pubUrl)
                                .replace("${" + appName + ".url}", pubUrl)
                                .replace("${" + appName + ".localUrl}", locUrl);
                    }
                }

                entry.setValue(resolvedValue);
            }
        }
    }
}