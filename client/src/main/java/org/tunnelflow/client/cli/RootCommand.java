package org.tunnelflow.client.cli;

import org.springframework.stereotype.Component;
import org.tunnelflow.client.cli.commands.LogsCommand;
import org.tunnelflow.client.cli.commands.StartCommand;
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
                ExposeCommand.class,
                StartCommand.class,
                LogsCommand.class
        }
)
@Component
public class RootCommand implements Runnable {

        @Override
        public void run() {
                System.out.println("""
                =============================================================
                  TunnelFlow v1.0.0
                  Secure tunneling & local application orchestration platform
                =============================================================

                Available Commands:
                  tunnelflow start         Start local dashboard & tunnel engine
                  tunnelflow start --debug Enable verbose debug logging
                  tunnelflow logs          Stream live request traffic logs
                  tunnelflow expose <port> Expose a local port instantly
                  tunnelflow app up        Launch multi-service application stack
                  tunnelflow version       Show version information

                Use --help on any command for detailed options.
                """);
        }
}