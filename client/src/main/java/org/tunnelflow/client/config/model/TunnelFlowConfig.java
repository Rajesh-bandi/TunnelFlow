package org.tunnelflow.client.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TunnelFlowConfig {

    private int version;

    private ProjectConfig project;

    private Map<String, ServiceConfig> services;

}
