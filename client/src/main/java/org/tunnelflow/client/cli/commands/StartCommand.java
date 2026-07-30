package org.tunnelflow.client.cli.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.BrowserLauncher;
import org.tunnelflow.client.service.TunnelConnectionManager;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor
@CommandLine.Command(
        name = "start",
        description = "Start the TunnelFlow local dashboard."
)
public class StartCommand implements Runnable {
    private final TunnelConnectionManager connectionManager;

    private static final String DASHBOARD_URL =
            "http://localhost:4040";

    private final BrowserLauncher browserLauncher;


    
    @Override
    public void run() {
        connectionManager.connect();
        System.out.println();
        System.out.println("TunnelFlow Dashboard");
        System.out.println(DASHBOARD_URL);
        System.out.println();
        System.out.println("Press Ctrl+C to stop TunnelFlow.");

        browserLauncher.open(DASHBOARD_URL);
    }
}