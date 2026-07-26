package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BrowserLauncher {

    public void open(String url) {

        String os = System.getProperty("os.name")
                .toLowerCase();

        try {

            ProcessBuilder processBuilder;

            if (os.contains("win")) {

                processBuilder = new ProcessBuilder(
                        "cmd",
                        "/c",
                        "start",
                        "",
                        url
                );

            } else if (os.contains("mac")) {

                processBuilder = new ProcessBuilder(
                        "open",
                        url
                );

            } else {

                processBuilder = new ProcessBuilder(
                        "xdg-open",
                        url
                );
            }

            processBuilder.start();

            log.debug("Opened dashboard: {}", url);

        } catch (Exception e) {

            log.warn(
                    "Could not open browser automatically. Open {} manually.",
                    url
            );
        }
    }
}