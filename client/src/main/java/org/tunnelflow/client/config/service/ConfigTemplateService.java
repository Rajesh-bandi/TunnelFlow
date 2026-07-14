package org.tunnelflow.client.config.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ConfigTemplateService {

    private static final String TEMPLATE = """
            version: 1

            project:
              name: my-app

            services:

              frontend:
                enabled: false
                path:
                command:
                port:

                env:

              backend:
                enabled: false
                path:
                command:
                port:

                env:

              postgres:
                enabled: false
                command:

                env:
            """;

    public boolean createTemplate() {

        Path configFile = Path.of("tunnelflow.yaml");

        if (Files.exists(configFile)) {
            log.warn("tunnelflow.yaml already exists.");
            return false;
        }

        try {

            Files.writeString(configFile, TEMPLATE);

            log.info("Created tunnelflow.yaml successfully.");

            return true;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to create tunnelflow.yaml",
                    e
            );

        }
    }
}