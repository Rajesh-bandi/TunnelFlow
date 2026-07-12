package org.tunnelflow.protocol.protocol.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisteredResponse {

    private String clientId;

    private String tunnelId;

    private String publicUrl;

}