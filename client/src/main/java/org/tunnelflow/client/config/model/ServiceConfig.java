package org.tunnelflow.client.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfig {

    private boolean enabled;

    private String type;

    private String path;

    private String command;

    private Integer port;

    private Map<String, String> env;

}
