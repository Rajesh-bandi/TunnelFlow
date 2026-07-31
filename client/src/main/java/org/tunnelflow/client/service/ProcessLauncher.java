package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.runtime.ApplicationRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
@Slf4j
public class ProcessLauncher {

    public ApplicationRuntime launch(ApplicationDeployment deployment) {
        String workingDirectory = deployment.getConfig().getPath();
        String command = deployment.getConfig().getCommand();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.environment().putAll(deployment.getResolvedEnvironment());
            processBuilder.directory(new File(workingDirectory));
            processBuilder.command(getCommand(command));

            log.info("Starting process [{}] in dir [{}] with cmd [{}]",
                    deployment.getConfig().getName(), workingDirectory, command);

            Process process = processBuilder.start();

            ApplicationRuntime runtime = new ApplicationRuntime(
                    deployment.getConfig(),
                    deployment.getTunnelRuntime(),
                    process
            );

            // Capture stdout
            Thread.startVirtualThread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        runtime.addLog(line);
                        log.info("[{}] {}", deployment.getConfig().getName(), line);
                    }
                } catch (IOException e) {
                    // Stream closed
                }
            });

            // Capture stderr
            Thread.startVirtualThread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        runtime.addLog("[STDERR] " + line);
                        log.error("[{}] {}", deployment.getConfig().getName(), line);
                    }
                } catch (IOException e) {
                    // Stream closed
                }
            });

            return runtime;

        } catch (IOException e) {
            throw new RuntimeException("Failed to start process for " + deployment.getConfig().getName(), e);
        }
    }

    private List<String> getCommand(String command) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String winCmd = command;
            if (winCmd.startsWith("./")) {
                winCmd = winCmd.substring(2);
            }
            return List.of("cmd", "/c", winCmd);
        }
        return List.of("sh", "-c", command);
    }
}