package org.tunnelflow.client.cli.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "version",
        description = "Show TunnelFlow version information."
)
public class VersionCommand implements Runnable {

    @Override
    public void run() {

        System.out.println("TunnelFlow CLI");
        System.out.println();

        System.out.println("Version : 1.0.0");
        System.out.println("Java    : " + System.getProperty("java.version"));
        System.out.println("OS      : " + System.getProperty("os.name"));
        System.out.println("Arch    : " + System.getProperty("os.arch"));
    }
}