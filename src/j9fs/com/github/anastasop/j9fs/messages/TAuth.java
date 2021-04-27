package com.github.anastasop.j9fs.messages;

public record TAuth(int tag, long afid, String uname, String aname) implements TMessage {}
