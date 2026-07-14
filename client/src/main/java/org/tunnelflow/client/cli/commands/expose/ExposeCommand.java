package org.tunnelflow.client.cli.commands.expose;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.service.TunnelCreationService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@RequiredArgsConstructor
@Component
@Command(
        name = "expose",
        description = "Expose a local port through TunnelFlow."
)
public class ExposeCommand implements Runnable {
    private final TunnelCreationService tunnelCreationService;
    @Parameters(
            index = "0",
            description = "Local port to expose."
    )
    private int port;

    @Override
    public void run() {
        String url = null;
        try {
            url = tunnelCreationService.createTunnel(port);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Tunnel URL: " + url);
        System.out.println("Creating tunnel for localhost:" + port);

    }

}