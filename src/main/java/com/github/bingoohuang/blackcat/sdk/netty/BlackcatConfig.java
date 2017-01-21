package com.github.bingoohuang.blackcat.sdk.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.val;

public class BlackcatConfig {
    public static final String HOST = System.getProperty("host", "127.0.0.1");
    public static final int PORT = Integer.parseInt(System.getProperty("port", "6667"));
    // Sleep 15 seconds before a reconnection attempt.
    public static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "15"));
    // Reconnect when the server sends nothing for 10 seconds.
    // public static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    @SneakyThrows
    public static SslContext configureSslForServer() {
        boolean SSL = System.getProperty("ssl") != null;
        if (!SSL) return null;

        val ssc = new SelfSignedCertificate();
        return SslContextBuilder
                .forServer(ssc.certificate(), ssc.privateKey())
                .build();
    }

    @SneakyThrows
    public static SslContext configureSslForClient() {
        boolean SSL = System.getProperty("ssl") != null;
        if (!SSL) return null;

        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }
}
