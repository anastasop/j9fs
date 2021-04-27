package com.github.anastasop.j9fs.messages;

public record TFlush(int tag, int oldtag) implements TMessage {}
