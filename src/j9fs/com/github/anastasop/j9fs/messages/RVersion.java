package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;
import com.github.anastasop.j9fs.protocol.Parameters;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record RVersion(int tag, long msize, String version) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        var bytes = version.getBytes(StandardCharsets.UTF_8);

        buffer.putInt(4 + 1 + 2 + 4 + 2 + bytes.length);
        buffer.put(Message.RVERSION);
        buffer.putShort((short) tag);
        buffer.putInt(Parameters.MAX_MESSAGE_SIZE);
        buffer.putShort((short)bytes.length);
        buffer.put(bytes);
    }
}
