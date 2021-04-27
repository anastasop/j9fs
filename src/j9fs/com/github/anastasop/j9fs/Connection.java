package com.github.anastasop.j9fs;

import com.github.anastasop.j9fs.messages.*;
import com.github.anastasop.j9fs.protocol.Parameters;
import com.github.anastasop.j9fs.protocol.ProtocolException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection {
    private final SocketChannel channel;

    private final Logger logger;

    private final ByteBuffer recvBuffer = ByteBuffer
            .allocate(Parameters.MAX_MESSAGE_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);

    private final ByteBuffer sendBuffer = ByteBuffer
            .allocate(Parameters.MAX_MESSAGE_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);

    private final MessageParser parser = new MessageParser(recvBuffer);

    private final LinkedList<RMessage> pendingResponses = new LinkedList<>();

    private final BiFunction<Long, String, FileServer> fsProvider;

    private int msize = 0;

    private FileServer tree;

    public Connection(SocketChannel channel, Logger logger, BiFunction<Long, String, FileServer> fsProvider) {
        this.channel = channel;
        this.fsProvider = fsProvider;
        this.logger = logger;
    }

    public int recv() throws IOException {
        var n = channel.read(recvBuffer);

        for (var msg = parser.nextMessage(); msg.isPresent(); msg = parser.nextMessage()) {
            msg.ifPresent(this::handleMessage);
        }

        return n;
    }

    public void send() throws IOException {
        sendBuffer.flip();
        if (sendBuffer.remaining() == 0) {
            sendBuffer.compact();
            if (pendingResponses.isEmpty()) {
                return;
            }
            var resp = pendingResponses.remove();
            getLogger().log(Level.INFO, "Response: {0}", resp);
            resp.write(sendBuffer);
            sendBuffer.flip();
        }
        channel.write(sendBuffer);
        sendBuffer.compact();
    }

    void handleMessage(TMessage msg) {
        getLogger().log(Level.INFO, "Request: {0}", msg);
        try {
            processMessage(msg);
        } catch (ProtocolException e) {
            pendingResponses.add(new RError(msg.tag(), e.getMessage()));
        }
    }

    void processMessage(TMessage msg) throws ProtocolException {
        if (msg instanceof TVersion version) {
            pendingResponses.clear();

            msize = Math.min((int)version.msize(), Parameters.MAX_MESSAGE_SIZE);
            var protocol = version.version().equals("9P2000") ? "9P2000" : "unknown";

            pendingResponses.add(new RVersion(version.tag(), msize, protocol));
        } else {
            if (msize == 0) {
                throw new ProtocolException("Version must be the first message on a connection");
            } else if (msg instanceof TAuth) {
                throw new ProtocolException("Auth is not required");
            } else if (msg instanceof TAttach attach) {
                try {
                    tree = fsProvider.apply(attach.fid(), attach.uname());
                    pendingResponses.add(new RAttach(msg.tag(), tree.getRoot().stat().qid()));
                } catch (Exception e) {
                    throw new ProtocolException(e.getMessage(), e);
                }
            } else if (msg instanceof TFlush flush) {
                var it = pendingResponses.iterator();
                while (it.hasNext()) {
                    if (it.next().tag() == flush.tag()) {
                        it.remove();
                        break;
                    }
                }
            } else {
                if (msg instanceof TOpen req) {
                    pendingResponses.add(tree.open(req));
                } else if (msg instanceof TCreate req) {
                    pendingResponses.add(tree.create(req));
                } else if (msg instanceof TClunk req) {
                    pendingResponses.add(tree.clunk(req));
                } else if (msg instanceof TWalk req) {
                    pendingResponses.add(tree.walk(req));
                } else if (msg instanceof TRead req) {
                    pendingResponses.add(tree.read(req));
                } else if (msg instanceof TWrite req) {
                    pendingResponses.add(tree.write(req));
                } else if (msg instanceof TStat req) {
                    pendingResponses.add(tree.stat(req));
                } else if (msg instanceof TWStat req) {
                    pendingResponses.add(tree.wstat(req));
                } else {
                    throw new ProtocolException("Request " + msg.getClass() + " not implemented yet");
                }
            }
        }
    }

    public void close() throws IOException {
        recvBuffer.clear();
        channel.close();
    }

    public SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    Logger getLogger() {
        return logger;
    }
}
