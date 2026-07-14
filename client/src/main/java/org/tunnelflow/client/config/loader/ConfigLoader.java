package org.tunnelflow.client.config.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.TunnelFlowConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigLoader {

    private final ObjectMapper yamlObjectMapper;

    public TunnelFlowConfig load(Path configFile) {

        if (!Files.exists(configFile)) {

            throw new RuntimeException(
                    "Configuration file not found: "
                            + configFile
                            + "\nRun 'tunnelflow app init' first."
            );

        }

        try {

            return yamlObjectMapper.readValue(
                    configFile.toFile(),
                    TunnelFlowConfig.class
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to parse configuration file: "
                            + configFile,
                    e
            );

        }

    }

}