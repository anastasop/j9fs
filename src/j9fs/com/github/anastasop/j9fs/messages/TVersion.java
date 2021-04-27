package com.github.anastasop.j9fs.messages;

public record TVersion(int tag, long msize, String version) implements TMessage {}
