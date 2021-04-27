package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;

import java.nio.ByteBuffer;

public record RWrite(int tag, long count) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(4 + 1 + 2 + 4);
        buffer.put(Message.RWRITE);
        buffer.putShort((short) tag);
        buffer.putInt((int)count);
    }
}
