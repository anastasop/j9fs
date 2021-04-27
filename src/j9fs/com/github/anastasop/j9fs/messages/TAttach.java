package com.github.anastasop.j9fs.messages;

public record TAttach(int tag, long fid, long afid, String uname, String aname) implements TMessage {}
