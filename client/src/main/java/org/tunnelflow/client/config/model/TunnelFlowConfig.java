package org.tunnelflow.client.config.model;

import lombok.Data;

import java.util.Map;

@Data
public class TunnelFlowConfig {

    private Map<String, ApplicationConfig> apps;

}