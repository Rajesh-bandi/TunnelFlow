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
    String id;

    MessageType type;

    String payload;

}
