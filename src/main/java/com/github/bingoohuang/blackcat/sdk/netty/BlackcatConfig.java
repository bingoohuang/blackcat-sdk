package com.github.bingoohuang.blackcat.sdk.netty;

import com.google.common.base.Throwables;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public class BlackcatConfig {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "6667"));
    // Sleep 5 seconds before a reconnection attempt.
    static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
    // Reconnect when the server sends nothing for 10 seconds.
    static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    public static SslContext configureSslForServer() {
        boolean SSL = System.getProperty("ssl") != null;
        if (!SSL) return null;

        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    static SslContext configureSslForClient() {
        boolean SSL = System.getProperty("ssl") != null;
        if (!SSL) return null;

        TrustManagerFactory instance = InsecureTrustManagerFactory.INSTANCE;
        try {
            return SslContextBuilder.forClient().trustManager(instance).build();
        } catch (SSLException e) {
            throw Throwables.propagate(e);
        }
    }
}
