package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.BlackcatClientConfig;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import com.google.common.net.HostAndPort;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.diamond.client.Miner;

import java.util.Properties;
import java.util.ServiceLoader;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.classpathInputStream;

public class BlackcatConfig {
    private static HostAndPort hostAndPort;
    // Sleep 15 seconds before a reconnection attempt.
    private static int reconnectDelay;
    // Reconnect when the server sends nothing for 10 seconds.
    // public static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    static {
        init();
    }

    public static HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public static int getReconnectDelay() {
        return reconnectDelay;
    }

    private static void init() {
        if (tryServiceLoader()) return;
        if (tryDiamond()) return;
        if (tryClasspathConfig()) return;

        trySystemProperties();
    }

    @SneakyThrows
    private static boolean tryClasspathConfig() {
        val is = classpathInputStream("blackcatserver.properties");
        if (is == null) return false;

        @Cleanup val bis = is;
        val properties = new Properties();
        properties.load(bis);

        String prop1 = properties.getProperty("hostAndPort", "127.0.0.1");
        hostAndPort = HostAndPort.fromString(prop1);

        String prop2 = properties.getProperty("reconnectDelay", "15");
        reconnectDelay = Integer.parseInt(prop2);

        return true;
    }

    private static boolean tryDiamond() {
        if (!Blackcats.hasDiamond) return false;

        val miner = new Miner().getMiner("blackcatserver", "config");
        if (miner == null) return false;

        String hostAndPortConfig = miner.getString("hostAndPort");
        if (hostAndPortConfig == null) return false;

        hostAndPort = HostAndPort.fromString(hostAndPortConfig);
        reconnectDelay = miner.getInt("reconnectDelay", 15);

        return true;
    }

    private static void trySystemProperties() {
        String prop1 = System.getProperty("blackcatserver.hostAndPort", "127.0.0.1:6667");
        hostAndPort = HostAndPort.fromString(prop1);

        String prop2 = System.getProperty("blackcatserver.reconnectDelay", "15");
        reconnectDelay = Integer.parseInt(prop2);
    }

    private static boolean tryServiceLoader() {
        val loader = ServiceLoader.load(BlackcatClientConfig.class);
        for (val config : loader) {
            val hostAndPortString = config.getHostAndPort();
            hostAndPort = HostAndPort.fromString(hostAndPortString);
            reconnectDelay = config.getReconnectDelay();
            return true;
        }

        return false;
    }

    @SneakyThrows
    public static SslContext configureSslForServer() {
        val ssl = System.getProperty("ssl") != null;
        if (!ssl) return null;

        val ssc = new SelfSignedCertificate();
        return SslContextBuilder
                .forServer(ssc.certificate(), ssc.privateKey())
                .build();
    }

    @SneakyThrows
    public static SslContext configureSslForClient() {
        val ssl = System.getProperty("ssl") != null;
        if (!ssl) return null;

        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }
}
