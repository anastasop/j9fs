package com.github.anastasop.j9fs.messages;

public record TWStat(int tag, long fid, Stat stat) implements TMessage {}
