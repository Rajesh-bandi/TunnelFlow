package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationDeployment;

import java.util.Map;

@Service
@Slf4j
public class PlaceholderResolver {

    public void resolve(
            Map<String, ApplicationDeployment> deployments
    ) {

        for (ApplicationDeployment deployment : deployments.values()) {

            // Copy configured environment into the working environment
            Map<String, String> configured =
                    deployment.getConfig().getEnv();

            if (configured != null) {
                deployment.getResolvedEnvironment().putAll(configured);
            }

            Map<String, String> environment =
                    deployment.getResolvedEnvironment();

            for (Map.Entry<String, String> entry : environment.entrySet()) {

                String value = entry.getValue();

                if (value == null) {
                    continue;
                }

                if (!value.startsWith("${") || !value.endsWith("}")) {
                    continue;
                }

                String placeholder =
                        value.substring(2, value.length() - 1);

                String[] parts = placeholder.split("\\.");

                if (parts.length != 2) {
                    continue;
                }

                String applicationName = parts[0];
                String property = parts[1];

                if (!property.equals("url")) {
                    continue;
                }

                ApplicationDeployment referenced =
                        deployments.get(applicationName);

                if (referenced == null) {
                    throw new RuntimeException(
                            "Unknown application: " + applicationName
                    );
                }

                if (referenced.getTunnelRuntime() == null) {
                    throw new RuntimeException(
                            "Tunnel not provisioned for application: "
                                    + applicationName
                    );
                }

                entry.setValue(
                        referenced.getTunnelRuntime().getPublicUrl()
                );
            }
        }
    }
}