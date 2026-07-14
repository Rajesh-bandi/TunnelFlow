package org.tunnelflow.protocol.protocol.tunnel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTunnelRequest {

    private int localPort;

}