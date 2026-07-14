package org.tunnelflow.client.cli.commands.expose;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tunnelflow.client.runtime.TunnelRuntime;
import org.tunnelflow.client.runtime.TunnelRuntimeRegistry;
import org.tunnelflow.client.service.ClientTunnelManager;
import org.tunnelflow.client.service.TunnelConnectionManager;
import org.tunnelflow.client.service.TunnelCreationService;
import org.tunnelflow.protocol.protocol.tunnel.TunnelCreatedResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@RequiredArgsConstructor
@Component
@Command(
        name = "expose",
        description = "Expose a local port through TunnelFlow."
)
public class ExposeCommand implements Runnable {

    private  final ClientTunnelManager clientTunnelManager;
    @Parameters(
            index = "0",
            description = "Local port to expose."
    )
    private int port;

    @Override
    public void run() {

        try {

            TunnelRuntime runtime =
                    clientTunnelManager.expose(port);

            System.out.println(
                    "Tunnel URL: " + runtime.getPublicUrl()
            );

            System.out.println(
                    "Creating tunnel for localhost:" + runtime.getLocalPort()
            );

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

}