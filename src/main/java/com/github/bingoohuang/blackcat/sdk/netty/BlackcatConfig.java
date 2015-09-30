package com.github.bingoohuang.blackcat.sdk.netty;

import com.google.common.base.Throwables;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public class BlackcatConfig {
    static final int PORT = Integer.parseInt(System.getProperty("port", "6667"));

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
