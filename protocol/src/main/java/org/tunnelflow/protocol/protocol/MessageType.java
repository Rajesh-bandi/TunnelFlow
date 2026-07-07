package org.tunnelflow.protocol.protocol;

public enum MessageType {
    CLIENT_REGISTER,
    CLIENT_REGISTERED,

    CREATE_TUNNEL,
    TUNNEL_CREATED,

    DELETE_TUNNEL,
    TUNNEL_DELETED,

    PING,
    PONG,

    HTTP_REQUEST,
    HTTP_RESPONSE
}
