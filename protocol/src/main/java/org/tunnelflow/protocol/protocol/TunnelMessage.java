package org.tunnelflow.protocol.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TunnelMessage {
    private String requestId;

    private MessageType type;

    private String clientId;

    private String tunnelId;

    private String payload;
}
