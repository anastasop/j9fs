package com.github.anastasop.j9fs.messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import com.github.anastasop.j9fs.protocol.Message;

public record RError(int tag, String ename) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        var bytes = ename.getBytes(StandardCharsets.UTF_8);

        buffer.putInt(4 + 1 + 2 + 2 + bytes.length);
        buffer.put(Message.RERROR);
        buffer.putShort((short) tag);
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
    }
}
