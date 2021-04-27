package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;
import java.nio.ByteBuffer;

public record RRead(int tag, ByteBuffer data) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        data.flip();
        buffer.putInt(4 + 1 + 2 + 4 + data.remaining());
        buffer.put(Message.RREAD);
        buffer.putShort((short) tag);
        buffer.putInt(data.remaining());
        buffer.put(data);
    }
}
