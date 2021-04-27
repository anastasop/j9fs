package com.github.anastasop.j9fs.server;

import com.github.anastasop.j9fs.Connection;
import com.github.anastasop.j9fs.FidDir;
import com.github.anastasop.j9fs.FileTree;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {
    private final Path rootPath;
    private final InetSocketAddress bindAddress;
    private final Selector selector;
    private final Logger logger;

    private static void usage() {
        System.err.println("""
Usage: j9fs [-h host] [-p port] -r root

J9fs is a 9P file server. It listens on host:post and serves the
directory root. It does not authenticate users and does not respect
file permissions. All files are rw and belong to user 'nobody'""");
        System.exit(2);
    }

    public static void main(String[] args) throws IOException {
        var ist = Server.class.getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(ist);
        ist.close();

        String hostname = null;
        int port = 8090;
        String rootPath = null;

        // very rudimentary argument parsing to avoid an external dependency
        var it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            switch (it.next()) {
                case "-r" -> rootPath = it.hasNext() ? it.next() : null;
                case "-h" -> hostname = it.hasNext() ? it.next() : null;
                case "-p" -> port = it.hasNext() ? Integer.parseInt(it.next()) : 8090;
                default -> usage();
            }
        }

        if (rootPath == null) {
            usage();
        }

        InetSocketAddress bindAddress = null;
        if (hostname == null) {
            bindAddress = new InetSocketAddress(port);
        } else {
            bindAddress = new InetSocketAddress(hostname, port);
        }

        new Server(bindAddress, Path.of(rootPath).normalize()).start();
    }

    private Server(InetSocketAddress bindAddress, Path rootPath) throws IOException {
        this.rootPath = rootPath;
        this.bindAddress = bindAddress;
        this.selector = Selector.open();
        this.logger = Logger.getLogger("com.github.anastasop.j9fs");
    }

    private void start() throws IOException {
        ServerSocketChannel listen_sock = ServerSocketChannel.open()
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(bindAddress);

        listen_sock
                .configureBlocking(false)
                .register(selector, SelectionKey.OP_ACCEPT);

        for (;;) {
            selector.select();

            var it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                var key = it.next();
                try {
                    handleKey(key);
                } catch(Exception e) {
                    getLogger().log(Level.SEVERE, "Server: ", e);
                    var conn = (Connection) key.attachment();
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch(Exception ex) {
                            getLogger().log(Level.SEVERE, "Server: ", ex);
                        }
                    }
                } finally {
                    it.remove();
                }
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            getLogger().log(Level.SEVERE, "Server: invalid key {0}", key);
        } else if (key.isAcceptable()) {
            SocketChannel conn_channel = (SocketChannel)
                    ((ServerSocketChannel)key.channel()).accept().configureBlocking(false);
            conn_channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                    new Connection(conn_channel, getLogger(),
                            (fid, fs) -> new FileTree(new FidDir(fid, rootPath), getLogger())));
            getLogger().log(Level.INFO, "Server: Accepted {0}", conn_channel.getRemoteAddress());
        } else if (key.isReadable()) {
            var conn = (Connection) key.attachment();
            if (conn.recv() == -1) {
                getLogger().log(Level.INFO, "Server: Closed {0}", conn.getRemoteAddress());
                conn.close();
            }
        } else if (key.isWritable()) {
            var conn = (Connection) key.attachment();
            conn.send();
        }
    }

    Logger getLogger() {
        return logger;
    }
}
