package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.*;
import com.github.anastasop.j9fs.protocol.ProtocolException;

public interface FileServer {
    Fid getRoot() throws ProtocolException;

    ROpen open(TOpen req) throws ProtocolException;

    RCreate create(TCreate req) throws ProtocolException;

    RClunk clunk(TClunk req) throws ProtocolException;

    RWalk walk(TWalk req) throws ProtocolException;

    RRead read(TRead req) throws ProtocolException;

    RWrite write(TWrite req) throws ProtocolException;

    RStat stat(TStat req) throws ProtocolException;

    RWStat wstat(TWStat req) throws ProtocolException;
}
