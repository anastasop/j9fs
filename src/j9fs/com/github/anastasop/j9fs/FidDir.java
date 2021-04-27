package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.Stat;
import com.github.anastasop.j9fs.protocol.FidPermissions;
import com.github.anastasop.j9fs.protocol.Parameters;
import com.github.anastasop.j9fs.protocol.ProtocolException;
import com.github.anastasop.j9fs.protocol.Qid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FidDir implements Fid {
    private final Long fid;
    private final Path path;
    private ByteBuffer statsBuffer;

    public FidDir(long fid, Path path) {
        this.fid = fid;
        this.path = path;
    }

    @Override
    public Long getFid() {
        return fid;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean isOpenForIO() {
        return statsBuffer != null;
    }

    @Override
    public void open(short mode) throws ProtocolException {
        var buf = ByteBuffer
                .allocate(Parameters.MAX_DIR_READ_MESSAGE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);

        try (var stream = Files.newDirectoryStream(getPath())) {
            for (var name: stream) {
                if (Files.isDirectory(name)) {
                    new FidDir(0, name).stat().write(buf);
                } else {
                    new FidFile(0, name).stat().write(buf);
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        statsBuffer = buf.flip();
    }

    @Override
    public void read(ByteBuffer dst, long offset, int count) throws ProtocolException {
        if (offset == 0) {
            statsBuffer.position(0);
        } else if (offset != statsBuffer.position()) {
            throw new ProtocolException("Read dir offset is not at end of previous read");
        }

        if (statsBuffer.hasRemaining()) {
            if (statsBuffer.remaining() < count) {
                dst.put(statsBuffer);
            } else {
                var oldLimit = statsBuffer.limit();
                statsBuffer.limit(statsBuffer.position() + count);
                dst.put(statsBuffer);
                statsBuffer.limit(oldLimit);
            }
        }
    }

    @Override
    public void write(ByteBuffer src, long offset) throws ProtocolException {
        throw new ProtocolException("can't write to a directory");
    }

    @Override
    public void close() throws ProtocolException {
        statsBuffer = null;
    }

    public Stat stat() throws ProtocolException {
        BasicFileAttributes attrs;
        try {
            attrs = Files.readAttributes(getPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch(Exception e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        var qid = Qid.of(path, attrs);
        long mode = (((long)qid.getType()) & 0xff) << 24;
        mode |= 0b111111111L & 0xFFF;

        return new Stat(
                0, 0, qid, mode,
                attrs.lastAccessTime().toMillis() / 1000, attrs.lastModifiedTime().toMillis() / 1000,
                attrs.size(), getPath().getFileName().toString(), Parameters.DEFAULT_USER, Parameters.DEFAULT_GROUP, Parameters.DEFAULT_USER
        );
    }

    @Override
    public Qid qid() throws ProtocolException {
        return stat().qid();
    }

    public Fid create(long fid, String name, long perms, short mode) throws ProtocolException {
        var path = getPath().resolve(name).normalize();

        try {
            return FidPermissions.DMDIR.includedIn(perms)?
                    new FidDir(fid, Files.createDirectory(path)) :
                    new FidFile(fid, Files.createFile(path));
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }
    }
}
