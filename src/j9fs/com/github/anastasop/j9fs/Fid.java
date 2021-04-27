package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.Stat;
import com.github.anastasop.j9fs.protocol.ProtocolException;
import com.github.anastasop.j9fs.protocol.Qid;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface Fid {
    Long getFid();

    Path getPath();

    boolean isOpenForIO();

    void open(short mode) throws ProtocolException;

    void read(ByteBuffer dst, long offset, int count) throws ProtocolException;

    void write(ByteBuffer src, long offset) throws ProtocolException;

    void close() throws ProtocolException;

    Stat stat() throws ProtocolException;

    Qid qid() throws ProtocolException;
}
