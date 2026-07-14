package org.tunnelflow.client.cli.commands.app;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "app",
        description = "Manage TunnelFlow applications.",
        subcommands = {
                InitCommand.class,
                UpCommand.class,
        }
)
public class AppCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'tunnelflow app --help' for available commands.");
    }
}