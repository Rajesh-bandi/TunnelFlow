package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProcessLauncher {
    public Process start(
            String workingDirectory,
            String command
    ) {
        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder();

            processBuilder.directory(
                    new File(workingDirectory)
            );

            processBuilder.command(
                    getCommand(command)
            );

            processBuilder.inheritIO();

            log.info("Starting process...");
            log.info("Directory : {}", workingDirectory);
            log.info("Command   : {}", command);

            return processBuilder.start();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to start process",
                    e
            );

        }
    }
    private List<String> getCommand(String command) {

        if (System.getProperty("os.name")
                .toLowerCase()
                .contains("windows")) {

            return List.of(
                    "cmd",
                    "/c",
                    command
            );
        }

        return List.of(
                "sh",
                "-c",
                command
        );
    }
}