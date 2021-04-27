package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record Stat(int type, long dev, Qid qid, long mode,
            long atime, long mtime, long length,
            String name, String uid, String gid, String muid) {
    public void write(ByteBuffer buffer) {
        byte[] bytes;

        var pos = buffer.position();
        buffer.putShort((short)0); // placeholder for size[2]
        buffer.putShort((short) type);
        buffer.putInt((int)dev);
        qid.write(buffer);
        buffer.putInt((int)mode);
        buffer.putInt((int)atime);
        buffer.putInt((int)mtime);
        buffer.putLong(length);
        bytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short)bytes.length);
        buffer.put(bytes);
        bytes = uid.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short)bytes.length);
        buffer.put(bytes);
        bytes = gid.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short)bytes.length);
        buffer.put(bytes);
        bytes = muid.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short)bytes.length);
        buffer.put(bytes);
        buffer.putShort(pos, (short)(buffer.position() - pos - 2)); // go back and fill size[2]
    }
}
