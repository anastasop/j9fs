package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;

import java.nio.ByteBuffer;

public record RWStat(int tag) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(4 + 1 + 2);
        buffer.put(Message.RWSTAT);
        buffer.putShort((short) tag);
    }
}
