package org.tunnelflow.client.config.model;

import lombok.Data;

import java.util.Map;

@Data
public class ApplicationConfig {

    private String name;

    private String workingDirectory;

    private String command;

    private int port;

    private Map<String, String> environment;
}