package org.tunnelflow.protocol.protocol.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterRequest {

    private String machineName;

    private String os;

    private String version;

}
