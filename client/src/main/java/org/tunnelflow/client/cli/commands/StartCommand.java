package org.tunnelflow.client.cli.commands;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.BrowserLauncher;
import org.tunnelflow.client.service.TunnelConnectionManager;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor
@CommandLine.Command(
        name = "start",
        description = "Start the TunnelFlow local dashboard & tunnel engine."
)
public class StartCommand implements Runnable {
    private final TunnelConnectionManager connectionManager;
    private final BrowserLauncher browserLauncher;

    private static final String DASHBOARD_URL = "http://localhost:4040";

    @CommandLine.Option(
            names = {"--debug", "-d"},
            description = "Enable verbose debug logging output."
    )
    private boolean debug = false;

    @CommandLine.Option(
            names = {"--no-browser"},
            description = "Skip opening web dashboard in browser."
    )
    private boolean noBrowser = false;

    @Override
    public void run() {
        if (debug) {
            enableDebugLogging();
            System.out.println("[DEBUG] Debug mode active -- verbose logging enabled.");
        }

        connectionManager.connect();

        if (!debug) {
            printCleanBanner();
        } else {
            System.out.println("TunnelFlow Engine started.");
            System.out.println("Dashboard: " + DASHBOARD_URL);
        }

        if (!noBrowser) {
            browserLauncher.open(DASHBOARD_URL);
        }
    }

    private void enableDebugLogging() {
        try {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);
            Logger tunnelflowLogger = (Logger) LoggerFactory.getLogger("org.tunnelflow");
            tunnelflowLogger.setLevel(Level.DEBUG);
        } catch (Exception e) {
            System.err.println("Could not enable debug logger: " + e.getMessage());
        }
    }

    private void printCleanBanner() {
        System.out.println();
        System.out.println("=============================================================");
        System.out.println();
        System.out.println("  TunnelFlow v1.0.0");
        System.out.println();
        System.out.println("  [+] Starting services...");
        System.out.println();
        System.out.println("  [OK] Local Dashboard");
        System.out.println("       " + DASHBOARD_URL);
        System.out.println();
        System.out.println("  [OK] Tunnel Engine Online");
        System.out.println("  [OK] Connected to TunnelFlow Server");
        System.out.println();
        System.out.println("  Ready.");
        System.out.println();
        System.out.println("  Press Ctrl+C to stop.");
        System.out.println();
        System.out.println("=============================================================");
        System.out.println();
    }
}