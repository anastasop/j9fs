package com.github.anastasop.j9fs.protocol;

public enum OpenMode {
    OREAD(0),
    OWRITE(1),
    ORDWR(2),
    OEXEC(3),
    OTRUNC(16),
    ORCLOSE(64);

    private short value;

    OpenMode(int value) {
        this.value = (short)(value & 0xFF);
    }

    public boolean includedIn(short mode) {
        return (value & (mode & 0xFF)) == value;
    }
}
