package org.tunnelflow.client.runtime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelRuntime {

    private String tunnelId;

    private int localPort;

    private String publicUrl;

}