package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.Stat;
import com.github.anastasop.j9fs.protocol.OpenMode;
import com.github.anastasop.j9fs.protocol.Parameters;
import com.github.anastasop.j9fs.protocol.ProtocolException;
import com.github.anastasop.j9fs.protocol.Qid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

class FidFile implements Fid {
    private final Long fid;
    private final Path path;
    private short openMode;
    private FileChannel channel;

    public FidFile(long fid, Path path) {
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
        return channel != null && channel.isOpen();
    }

    @Override
    public void open(short mode) throws ProtocolException {
        if (isOpenForIO()) {
            throw new ProtocolException("File is already open");
        }

        var openOptions = new OpenOption[8];
        var i = 0;
        if (OpenMode.OEXEC.includedIn(mode)) {
            openOptions[i++] = StandardOpenOption.READ;
        } else if (OpenMode.ORDWR.includedIn(mode)) {
            openOptions[i++] = StandardOpenOption.READ;
            openOptions[i++] = StandardOpenOption.WRITE;
        } else {
            if (OpenMode.OREAD.includedIn(mode)) {
                openOptions[i++] = StandardOpenOption.READ;
            }
            if (OpenMode.OWRITE.includedIn(mode)) {
                openOptions[i++] = StandardOpenOption.WRITE;
            }
        }
        if (OpenMode.OTRUNC.includedIn(mode)) {
            openOptions[i++] = StandardOpenOption.TRUNCATE_EXISTING;
        }
        if (OpenMode.ORCLOSE.includedIn(mode)) {
            openOptions[i++] = StandardOpenOption.DELETE_ON_CLOSE;
        }

        try {
            channel = FileChannel.open(getPath(), Arrays.copyOf(openOptions, i));
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        openMode = mode;
    }

    @Override
    public void read(ByteBuffer dst, long offset, int count) throws ProtocolException {
        if (!isOpenForIO()) {
            throw new ProtocolException("File is not open for IO");
        }
        if (!OpenMode.ORDWR.includedIn(openMode) && !OpenMode.OREAD.includedIn(openMode)) {
            throw new ProtocolException("File is not open for reading");
        }

        try {
            channel.read(dst, offset);
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }
    }

    @Override
    public void write(ByteBuffer src, long offset) throws ProtocolException {
        if (!isOpenForIO()) {
            throw new ProtocolException("File is not open for IO");
        }
        if (!OpenMode.ORDWR.includedIn(openMode) && !OpenMode.OWRITE.includedIn(openMode)) {
            throw new ProtocolException("File is not open for writing");
        }

        try {
            channel.position(offset);
            channel.write(src);
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws ProtocolException {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                throw new ProtocolException(e.getMessage(), e);
            }
        }
    }

    public Stat stat() throws ProtocolException {
        BasicFileAttributes attrs;
        try {
            attrs = Files.readAttributes(getPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        var qid = Qid.of(path, attrs);
        long mode = (((long)qid.getType()) & 0xff) << 24;
        mode |= 0b110110110L & 0xFFF;

        return new Stat(
                0, 0, qid, mode,
                attrs.lastAccessTime().toMillis() / 1000, attrs.lastModifiedTime().toMillis() / 1000,
                attrs.size(), getPath().getFileName().toString(), Parameters.DEFAULT_USER, Parameters.DEFAULT_GROUP, Parameters.DEFAULT_USER
        );
    }

    public Qid qid() throws ProtocolException {
        return stat().qid();
    }
}
