package com.github.anastasop.j9fs.protocol;

public final class Message {
    public static final byte TVERSION = 100;
    public static final byte RVERSION = 101;
    public static final byte TAUTH    = 102;
    public static final byte RAUTH    = 103;
    public static final byte TATTACH  = 104;
    public static final byte RATTACH  = 105;
    public static final byte TERROR   = 106; // Placeholder. Not used.
    public static final byte RERROR   = 107;
    public static final byte TFLUSH   = 108;
    public static final byte RFLUSH   = 109;
    public static final byte TWALK    = 110;
    public static final byte RWALK    = 111;
    public static final byte TOPEN    = 112;
    public static final byte ROPEN    = 113;
    public static final byte TCREATE  = 114;
    public static final byte RCREATE  = 115;
    public static final byte TREAD    = 116;
    public static final byte RREAD    = 117;
    public static final byte TWRITE   = 118;
    public static final byte RWRITE   = 119;
    public static final byte TCLUNK   = 120;
    public static final byte RCLUNK   = 121;
    public static final byte TREMOVE  = 122;
    public static final byte RREMOVE  = 123;
    public static final byte TSTAT    = 124;
    public static final byte RSTAT    = 125;
    public static final byte TWSTAT   = 126;
    public static final byte RWSTAT   = 127;
}
