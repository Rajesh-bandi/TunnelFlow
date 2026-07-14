package org.tunnelflow.client.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tunnelflow.client.config.model.ApplicationConfig;

@Data
@AllArgsConstructor
public class ApplicationRuntime {

    private final ApplicationConfig config;

    private final TunnelRuntime tunnelRuntime;

    private final Process process;

}
