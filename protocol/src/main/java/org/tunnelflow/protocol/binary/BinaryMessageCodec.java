package org.tunnelflow.protocol.binary;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

public class BinaryMessageCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static byte[] encode(Object header, byte[] body) throws Exception {
        byte[] headerBytes = OBJECT_MAPPER.writeValueAsBytes(header);
        byte[] payloadBody = body != null ? body : new byte[0];

        ByteBuffer buffer = ByteBuffer.allocate(4 + headerBytes.length + payloadBody.length);
        buffer.putInt(headerBytes.length);
        buffer.put(headerBytes);
        buffer.put(payloadBody);

        return buffer.array();
    }

    public static <T> BinaryDecodedMessage<T> decode(byte[] binaryData, Class<T> headerClass) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(binaryData);
        int headerLength = buffer.getInt();

        byte[] headerBytes = new byte[headerLength];
        buffer.get(headerBytes);

        byte[] bodyBytes = new byte[buffer.remaining()];
        buffer.get(bodyBytes);

        T header = OBJECT_MAPPER.readValue(headerBytes, headerClass);

        return new BinaryDecodedMessage<>(header, bodyBytes);
    }

    public static class BinaryDecodedMessage<T> {
        private final T header;
        private final byte[] body;

        public BinaryDecodedMessage(T header, byte[] body) {
            this.header = header;
            this.body = body;
        }

        public T getHeader() {
            return header;
        }

        public byte[] getBody() {
            return body;
        }
    }
}
