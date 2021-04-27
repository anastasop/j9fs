package com.github.anastasop.j9fs.messages;

public record TRead(int tag, long fid, long offset, long count) implements TMessage {}
