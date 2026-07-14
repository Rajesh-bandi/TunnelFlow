package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tunnelflow.client.config.model.ApplicationDeployment;
import org.tunnelflow.client.runtime.ApplicationRuntime;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProcessLauncher {
    public ApplicationRuntime launch(
            ApplicationDeployment deployment
    ){
        String workingDirectory =
                deployment.getConfig().getWorkingDirectory();

        String command =
                deployment.getConfig().getCommand();
        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder();
            processBuilder.environment().putAll(
                    deployment.getResolvedEnvironment()
            );
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

            Process process =
                    processBuilder.start();
            return new ApplicationRuntime(
                    deployment.getConfig(),
                    deployment.getTunnelRuntime(),
                    process
            );

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