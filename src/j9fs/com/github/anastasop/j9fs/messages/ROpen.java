package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;
import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;

public record ROpen(int tag, Qid qid, int iounit) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(4 + 1 + 2 + Qid.SIZE + 4);
        buffer.put(Message.ROPEN);
        buffer.putShort((short) tag);
        qid.write(buffer);
        buffer.putInt(iounit);
    }
}
