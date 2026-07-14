package org.tunnelflow.client.cli.commands.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.ApplicationManager;
import picocli.CommandLine;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@CommandLine.Command(
        name = "up",
        description = "Start a TunnelFlow application."
)
public class UpCommand implements Runnable {

    private final ApplicationManager applicationManager;

    @Override
    public void run() {

        Path configPath = Path.of("tunnelflow.yaml");

        applicationManager.up(configPath);

    }

}