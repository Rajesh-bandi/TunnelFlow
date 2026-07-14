package org.tunnelflow.client.cli.commands.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.config.service.ConfigTemplateService;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor
@CommandLine.Command(
        name = "init",
        description = "Create a new TunnelFlow configuration file."
)
public class InitCommand implements Runnable {

    private final ConfigTemplateService configTemplateService;

    @Override
    public void run() {

        boolean created =
                configTemplateService.createTemplate();

        if (created) {
            System.out.println("✔ Created tunnelflow.yaml");
        } else {
            System.out.println("⚠ tunnelflow.yaml already exists.");
        }

    }

}