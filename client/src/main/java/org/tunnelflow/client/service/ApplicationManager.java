package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.loader.ConfigLoader;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.config.model.TunnelFlowConfig;
import org.tunnelflow.client.config.validation.ConfigValidator;
import org.tunnelflow.client.config.validation.ValidationResult;
import org.tunnelflow.client.runtime.ApplicationRuntime;
import org.tunnelflow.client.runtime.ApplicationRuntimeRegistry;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;

import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationManager {

    private final TunnelConnectionManager tunnelConnectionManager;
    private final ConfigLoader configLoader;
    private final ConfigValidator configValidator;
    private final TunnelProvisioner tunnelProvisioner;
    private final PlaceholderResolver placeholderResolver;
    private final ProcessLauncher processLauncher;

    private final ApplicationRuntimeRegistry applicationRuntimeRegistry;
    private final TunnelRuntimeRegistry tunnelRuntimeRegistry;

    public void up(Path configPath) {

        tunnelConnectionManager.connect();

        log.info("Starting application...");

        TunnelFlowConfig config = loadConfiguration(configPath);

        validateConfiguration(config);

        Map<String, ApplicationDeployment> deployments =
                tunnelProvisioner.provision(config);

        placeholderResolver.resolve(deployments);

        launchApplications(deployments);

        log.info("Application startup completed.");
    }

    private TunnelFlowConfig loadConfiguration(Path configPath) {
        return configLoader.load(configPath);
    }

    private void validateConfiguration(TunnelFlowConfig config) {

        ValidationResult result = configValidator.validate(config);

        if (!result.isValid()) {

            result.getErrors().forEach(error ->
                    log.error("{} : {}", error.getField(), error.getMessage()));

            throw new RuntimeException("Configuration validation failed.");
        }
    }

    private void launchApplications(
            Map<String, ApplicationDeployment> deployments
    ) {

        deployments.forEach((name, deployment) -> {

            ApplicationRuntime runtime =
                    processLauncher.launch(deployment);

            // Register application runtime
            applicationRuntimeRegistry.register(name, runtime);

            // Register tunnel runtime for HTTP forwarding
            tunnelRuntimeRegistry.register(runtime.getTunnelRuntime());

            log.info("{} started successfully", name);

        });

    }
}