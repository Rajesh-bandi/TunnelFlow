package org.tunnelflow.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.loader.ConfigLoader;
import org.tunnelflow.client.config.model.ServiceConfig;
import org.tunnelflow.client.config.model.TunnelFlowConfig;
import org.tunnelflow.client.config.validation.ConfigValidator;
import org.tunnelflow.client.config.validation.ValidationResult;

import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupService {
    private final ProcessLauncher processLauncher;
    private final ConfigLoader configLoader;
    private final ConfigValidator configValidator;

    public void start() {

        log.info("Starting application...");

        Path configPath = Path.of("tunnelflow.yaml");

        TunnelFlowConfig config =
                configLoader.load(configPath);
        ValidationResult result =
                configValidator.validate(config);
        if (!result.isValid()) {

            result.getErrors().forEach(error ->
                    log.error("{} : {}", error.getField(), error.getMessage()));

            throw new RuntimeException("Configuration validation failed.");
        }
        log.info("Services found: {}", config.getServices().size());

        for (Map.Entry<String, ServiceConfig> entry : config.getServices().entrySet()) {

            log.info("Processing service: {}", entry.getKey());

            ServiceConfig service = entry.getValue();

            if (!service.isEnabled()) {
                log.info("{} is disabled", entry.getKey());
                continue;
            }

            log.info("Launching {}", entry.getKey());

            processLauncher.start(
                    service.getPath(),
                    service.getCommand()
            );

            log.info("{} launched", entry.getKey());
        }

        log.info("Project: {}", config.getProject().getName());

    }
}