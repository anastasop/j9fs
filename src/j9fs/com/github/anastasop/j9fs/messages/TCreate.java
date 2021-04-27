package com.github.anastasop.j9fs.messages;

public record TCreate(int tag, long fid, String name, long perm, short mode) implements TMessage {}
