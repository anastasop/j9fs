package com.github.anastasop.j9fs.messages;

import java.util.List;

public record TWalk(int tag, long fid, long newfid, List<String> names) implements TMessage {}
