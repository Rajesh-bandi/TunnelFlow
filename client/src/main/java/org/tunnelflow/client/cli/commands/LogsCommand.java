package org.tunnelflow.client.cli.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Command(
        name = "logs",
        description = "Stream live request logs from the running TunnelFlow instance."
)
public class LogsCommand implements Runnable {

    @Option(names = {"--follow", "-f"}, description = "Follow live request stream continuously", defaultValue = "true")
    private boolean follow = true;

    @Override
    public void run() {
        System.out.println();
        System.out.println("=============================================================");
        System.out.println("  TunnelFlow Live Request Traffic Stream");
        System.out.println("=============================================================");
        System.out.println();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        String lastReqId = "";

        do {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4040/api/requests"))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();
                    if (body != null && !body.equals("[]") && !body.isEmpty()) {
                        if (!body.equals(lastReqId)) {
                            lastReqId = body;
                            System.out.println("[*] Live Traffic Stream:");
                            System.out.println(body);
                            System.out.println();
                        }
                    } else {
                        System.out.println("[*] Listening for tunneled requests... (No requests recorded yet)");
                    }
                } else {
                    System.out.println("[!] Failed to connect to local TunnelFlow instance at http://localhost:4040");
                    break;
                }

                if (follow) {
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                System.out.println("[X] TunnelFlow server is not running. Start it first with 'tunnelflow start'.");
                break;
            }
        } while (follow);
    }
}
