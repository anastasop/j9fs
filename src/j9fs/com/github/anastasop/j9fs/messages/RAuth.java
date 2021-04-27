package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;
import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;

public record RAuth(int tag, Qid qid) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(4 + 1 + 2 + Qid.SIZE);
        buffer.put(Message.RAUTH);
        buffer.putShort((short)tag);
        qid.write(buffer);
    }
}
