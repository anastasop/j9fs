package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Message;
import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;
import java.util.List;

public record RWalk(int tag, List<Qid> qids) implements RMessage {
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(4 + 1 + 2 + 2 + qids.size() * Qid.SIZE);
        buffer.put(Message.RWALK);
        buffer.putShort((short) tag);
        buffer.putShort((short) qids.size());
        for (var qid: qids) {
            qid.write(buffer);
        }
    }
}
