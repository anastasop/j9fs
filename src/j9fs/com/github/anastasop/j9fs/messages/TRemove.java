package com.github.anastasop.j9fs.messages;

public record TRemove(int tag, long fid) implements TMessage {}
