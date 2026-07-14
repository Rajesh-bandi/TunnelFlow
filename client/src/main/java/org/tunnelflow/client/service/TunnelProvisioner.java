package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.config.model.ApplicationConfig;
import org.tunnelflow.client.config.model.TunnelFlowConfig;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TunnelProvisioner {

    private final TunnelCreationService tunnelCreationService;

    public Map<String, ApplicationDeployment> provision(
            TunnelFlowConfig config
    ) {

        log.info("Provisioning applications...");

        Map<String, ApplicationDeployment> deployments =
                new LinkedHashMap<>();

        config.getApps().forEach((name, app) -> {

            app.setName(name);

            ApplicationDeployment deployment =
                    new ApplicationDeployment(app);

            deployment.setTunnelRuntime(createTunnelRuntime(app));

            deployments.put(name, deployment);

        });

        return deployments;
    }

    private TunnelRuntime createTunnelRuntime(ApplicationConfig app) {

        try {

            TunnelCreatedResponse response =
                    tunnelCreationService.createTunnel(app.getPort());

            return TunnelRuntime.builder()
                    .tunnelId(response.getTunnelId())
                    .publicUrl(response.getPublicUrl())
                    .localPort(app.getPort())
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to provision tunnel for application: "
                            + app.getName(),
                    e
            );

        }

    }

}