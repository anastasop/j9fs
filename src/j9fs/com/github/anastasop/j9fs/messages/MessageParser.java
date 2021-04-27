package com.github.anastasop.j9fs.messages;

import com.github.anastasop.j9fs.messages.*;
import com.github.anastasop.j9fs.protocol.Message;
import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageParser {
    private final ByteBuffer buffer;

    public MessageParser(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Optional<TMessage> nextMessage() {
        TMessage msg = null;

        buffer.flip();
        if (buffer.remaining() >= 4) {
            buffer.mark();
            long msize = readSz4();
            if (buffer.remaining() < msize - 4) {
                buffer.reset();
            } else {
                msg = switch (readByte()) {
                    case Message.TAUTH -> new TAuth(readTag(), readFid(), readString(), readString());
                    case Message.TATTACH -> new TAttach(readTag(), readFid(), readFid(), readString(), readString());
                    case Message.TCLUNK -> new TClunk(readTag(), readFid());
                    case Message.TFLUSH -> new TFlush(readTag(), readTag());
                    case Message.TOPEN -> new TOpen(readTag(), readFid(), readByteS());
                    case Message.TCREATE -> new TCreate(readTag(), readFid(), readString(), readPerm(), readByteS());
                    case Message.TREAD -> new TRead(readTag(), readFid(), readSz8(), readSz4());
                    case Message.TWRITE -> new TWrite(readTag(), readFid(), readSz8(), readBytes(readSz4()));
                    case Message.TREMOVE -> new TRemove(readTag(), readFid());
                    case Message.TSTAT -> new TStat(readTag(), readFid());
                    case Message.TVERSION -> new TVersion(readTag(), readSz4(), readString());
                    case Message.TWALK -> new TWalk(readTag(), readFid(), readFid(), readListString());
                    case Message.TWSTAT -> new TWStat(readTag(), readFid(), readStat());
                    default -> throw new RuntimeException("Unsupported message type");
                };
            }
        }
        buffer.compact();

        return Optional.ofNullable(msg);
    }

    private byte readByte() {
        return buffer.get();
    }

    private int readTag() {
        return readSz2();
    }

    private long readFid() {
        return readSz4();
    }

    private long readPerm() {
        return readSz4();
    }

    private String readString() {
        var siz = readSz2();
        var bytes = new byte[siz];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private List<String> readListString() {
        var num = readSz2();
        var strings = new ArrayList<String>(num);
        for (var i = 0; i < num; i++) {
            strings.add(readString());
        }
        return strings;
    }

    private short readByteS() {
        return (short)((int)readByte() & 0xFF);
    }
    private int readByteI() {
        return (int)readByte() & 0xFF;
    }
    private long readByteL() {
        return (long)readByte() & 0xFF;
    }

    private int readSz2() {
        return readByteI() | readByteI() << 8;
    }

    private long readSz4() {
        return readByteL() | readByteL() << 8 | readByteL() << 16 | readByteL() << 24;
    }

    private long readSz8() {
        var val1 = readSz4();
        var val2 = readSz4();

        return val1;
    }

    private ByteBuffer readBytes(long count) {
        var buf = ByteBuffer.allocate((int) count).order(ByteOrder.LITTLE_ENDIAN);
        for (var i = 0; i < count ;i++) {
            buf.put(buffer.get());
        }
        buf.flip();
        return buf;
    }

    private Stat readStat() {
        var count = readSz2();

        var size = readSz2();
        var type = readSz2();
        var dev = readSz4();
        var qidType = readByte();
        var qidVersion = readSz4();
        var qidPath = readSz8();
        var mode = readSz4();
        var atime = readSz4();
        var mtime = readSz4();
        var length = readSz8();
        var name = readString();
        var uid = readString();
        var gid = readString();
        var muid = readString();

        return new Stat(
                type, dev, new Qid(qidType, qidType, qidVersion),
                mode, atime, mtime, length,
                name, uid, gid, muid
        );
    }
}
