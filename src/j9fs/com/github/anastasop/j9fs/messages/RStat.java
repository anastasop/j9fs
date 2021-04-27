package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;

import java.nio.ByteBuffer;

public record RStat(int tag, Stat stat) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        var pos = buffer.position();
        buffer.putInt(0); // placeholder for size[2] of message
        buffer.put(Message.RSTAT);
        buffer.putShort((short) tag);
        var pos2 = buffer.position();
        buffer.putShort((short)0); // placeholder for size[2] of stat
        stat.write(buffer);
        buffer.putShort(pos2, (short)(buffer.position() - pos2 - 2));
        buffer.putInt(pos, buffer.position() - pos);
    }
}
