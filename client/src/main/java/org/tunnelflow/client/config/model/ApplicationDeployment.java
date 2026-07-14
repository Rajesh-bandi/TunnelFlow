package org.tunnelflow.client.config.model;

import lombok.Data;
import org.tunnelflow.client.config.model.ApplicationConfig;
import org.tunnelflow.client.runtime.TunnelRuntime;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ApplicationDeployment {

    private final ApplicationConfig config;

    private TunnelRuntime tunnelRuntime;

    private Map<String,String> resolvedEnvironment =
            new LinkedHashMap<>();

}