package com.github.anastasop.j9fs.messages;

public record TOpen(int tag, long fid, short mode) implements TMessage {}
