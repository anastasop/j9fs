package com.github.anastasop.j9fs.messages;

import java.nio.ByteBuffer;

public record TWrite(int tag, long fid, long offset, ByteBuffer data) implements TMessage {}
