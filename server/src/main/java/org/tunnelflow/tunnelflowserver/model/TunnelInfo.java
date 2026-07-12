package org.tunnelflow.tunnelflowserver.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelInfo {

    private String tunnelId;

    private String clientId;

}
