package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.*;
import com.github.anastasop.j9fs.protocol.Parameters;
import com.github.anastasop.j9fs.protocol.ProtocolException;
import com.github.anastasop.j9fs.protocol.Qid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class FileTree implements FileServer {
    private final Map<Long, Fid> fids = new HashMap<>();
    private final Fid rootFid;
    private final Logger logger;

    public FileTree(Fid rootFid, Logger logger) {
        this.rootFid = rootFid;
        this.logger = logger;
        addFid(rootFid);
    }

    private void addFid(Fid fid) {
        fids.put(fid.getFid(), fid);
    }

    private Fid getFid(long fid) {
        return fids.get(fid);
    }

    private void removeFid(Fid fid) { fids.remove(fid.getFid()); }

    @Override
    public Fid getRoot() {
        return rootFid;
    }

    @Override
    public ROpen open(TOpen req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Predicate.not(Fid::isOpenForIO), "is already open");

        fid.open(req.mode());

        return new ROpen(req.tag(), fid.qid(), Parameters.IO_UNIT);
    }

    @Override
    public RCreate create(TCreate req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Predicate.not(Fid::isOpenForIO), "fid is open for IO");
        assertForFid(req.fid(), fid, FidDir.class::isInstance, "fid is not a directory");

        var newFid = ((FidDir)fid).create(req.fid(), req.name(), req.perm(), req.mode());
        newFid.open(req.mode());
        addFid(newFid);

        return new RCreate(req.tag(), newFid.qid(), Parameters.IO_UNIT);
    }

    @Override
    public RRead read(TRead req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Fid::isOpenForIO, "is not open");

        var dst = ByteBuffer.allocate((int)req.count());
        fid.read(dst, req.offset(), (int)req.count());

        return new RRead(req.tag(), dst);
    }

    @Override
    public RWrite write(TWrite req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Fid::isOpenForIO, "is not open");

        var count = req.data().remaining();
        fid.write(req.data(), req.offset());

        return new RWrite(req.tag(), count);
    }

    @Override
    public RClunk clunk(TClunk req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "not registered");

        fid.close();
        removeFid(fid);

        return new RClunk(req.tag());
    }

    @Override
    public RStat stat(TStat req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");

        return new RStat(req.tag(), fid.stat());
    }

    @Override
    public RWStat wstat(TWStat req) throws ProtocolException {
        var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Predicate.not(Fid::isOpenForIO), "is open for IO");

        if (!req.stat().name().isEmpty()) {
            var newPath = fid.getPath().resolve("..").resolve(req.stat().name());
            if (Files.exists(newPath)) {
                throw new ProtocolException("Twstat cannot rename to an existing file");
            }

            try {
                Files.move(fid.getPath(), newPath);
            } catch (IOException e) {
                throw new ProtocolException(e.getMessage(), e);
            }

            removeFid(fid);
            if (fid instanceof FidFile) {
                fid = new FidFile(fid.getFid(), fid.getPath());
            } else if (fid instanceof FidDir) {
                fid = new FidDir(fid.getFid(), fid.getPath());
            }
            addFid(fid);
        }
        // TODO: length, mtime

        return new RWStat(req.tag());
    }

    @Override
    public RWalk walk(TWalk req) throws ProtocolException {
        final var fid = getFid(req.fid());

        assertForFid(req.fid(), fid, Objects::nonNull, "unknown");
        assertForFid(req.fid(), fid, Predicate.not(Fid::isOpenForIO), "is open for IO");
        if (req.newfid() != req.fid()) {
            assertForFid(req.newfid(), getFid(req.newfid()), Objects::isNull, "new fid already in use");
        }
        if (!req.names().isEmpty()) {
            assertForFid(req.fid(), fid, FidDir.class::isInstance, "is not a directory");
        }

        var qIds = new ArrayList<Qid>(req.names().size());
        var newPath = fid.getPath();
        for (String name : req.names()) {
            try {
                newPath = newPath.resolve(name).normalize();
                qIds.add(Qid.of(newPath));
            } catch (InvalidPathException | IOException e) {
                break;
            }
        }

        if (qIds.size() == req.names().size()) {
            if (Files.isDirectory(newPath)) { // TODO use qIds last
                addFid(new FidDir(req.newfid(), newPath));
            } else {
                addFid(new FidFile(req.newfid(), newPath));
            }
        }

        return new RWalk(req.tag(), qIds);
    }

    private void assertForFid(long fidNum, Fid fid, Predicate<Fid> assertion, String msg) throws ProtocolException {
        if (!assertion.test(fid)) {
            throw new ProtocolException("Fid " + fidNum + ": " + msg);
        }
    }

    private Logger getLogger() {
        return logger;
    }
}
