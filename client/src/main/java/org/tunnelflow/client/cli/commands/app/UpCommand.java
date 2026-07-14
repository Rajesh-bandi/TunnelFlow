package org.tunnelflow.client.cli.commands.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.config.loader.ConfigLoader;
import org.tunnelflow.client.config.model.TunnelFlowConfig;
import org.tunnelflow.client.service.ApplicationStartupService;
import picocli.CommandLine;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@CommandLine.Command(
        name = "up",
        description = "Start a TunnelFlow application."
)
public class UpCommand implements Runnable {
    private final ApplicationStartupService applicationStartupService;

    @Override
    public void run() {

        applicationStartupService.start();

    }

}
