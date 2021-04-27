package com.github.anastasop.j9fs.protocol;

public enum FidPermissions {
    DMDIR(0x80000000),
    DMAPPEND(0x40000000),
    DMEXCL(0x20000000),
    DMAUTH(0x08000000),
    DMTMP(0x04000000);

    private long value;

    FidPermissions(long value) { this.value = value; }

    public boolean includedIn(long perms) { return (value & perms) != 0L; }

    public long value() { return value; }
}
