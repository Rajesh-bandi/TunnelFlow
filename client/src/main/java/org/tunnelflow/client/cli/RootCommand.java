package org.tunnelflow.client.cli;

import org.springframework.stereotype.Component;
import org.tunnelflow.client.cli.commands.VersionCommand;
import org.tunnelflow.client.cli.commands.app.AppCommand;
import org.tunnelflow.client.cli.commands.expose.ExposeCommand;
import picocli.CommandLine.Command;

@Command(
        name = "tunnelflow",
        mixinStandardHelpOptions = true,
        version = "TunnelFlow 1.0.0",
        description = "Expose local applications securely through TunnelFlow.",
        subcommands = {
                VersionCommand.class,
                AppCommand.class,
                ExposeCommand.class
        }
)
@Component
public class RootCommand implements Runnable {

        @Override
        public void run() {

                System.out.println("""
                TunnelFlow CLI

                Use --help to see available commands.
                """);

        }
}