package com.github.anastasop.j9fs.messages;

import java.nio.ByteBuffer;

public interface RMessage {
    int tag();
    void write(ByteBuffer buffer);
}
