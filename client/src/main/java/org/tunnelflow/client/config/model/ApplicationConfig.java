package org.tunnelflow.client.config.model;

import lombok.Data;

import java.util.Map;

@Data
public class ApplicationConfig {

    private String name;

    private boolean enabled;

    private String path;

    private String command;

    private int port;

    private Map<String, String> env;

}