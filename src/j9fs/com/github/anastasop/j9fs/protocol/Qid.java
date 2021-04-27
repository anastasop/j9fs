package com.github.anastasop.j9fs.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class Qid {
    public static final int SIZE = 13;

    public static final short QTDIR = 0x80;
    public static final short QTAPPEND = 0x40;
    public static final short QTEXCL = 0x20;
    public static final short QTAUTH = 0x08;
    public static final short QTTMP = 0x04;
    public static final short QTFILE = 0x00;

    private static final LinkOption[] DEFAULT_LINK_OPTIONS = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};

    private final short type;
    private final long version;
    private final long path;

    public short getType() {
        return type;
    }

    public long getVersion() {
        return version;
    }

    public long getPath() {
        return path;
    }

    public Qid(short type, long version, long path) {
        this.type = type;
        this.version = version;
        this.path = path;
    }

    public static Qid of(Path path, BasicFileAttributes attrs) {
        short type = attrs.isDirectory() ? Qid.QTDIR : QTFILE;

        long version = 1L;

        long inumber = attrs.fileKey() != null ? attrs.fileKey().hashCode() : path.hashCode();

        return new Qid(type, version, inumber);
    }

    public static Qid of(Path path) throws IOException {
        return of(path, Files.readAttributes(path, BasicFileAttributes.class, DEFAULT_LINK_OPTIONS));
    }

    public void write(ByteBuffer buffer) {
        buffer.put((byte)type);
        buffer.putInt((int)version);
        buffer.putLong(path);
    }
}
