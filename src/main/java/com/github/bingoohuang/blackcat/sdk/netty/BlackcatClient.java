package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg;
import com.google.common.base.Throwables;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;

public final class BlackcatClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final BlackcatClientHandler blackcatClientHandler = init();

    public static void send(BlackcatMsg.BlackcatMsgReq req) {
        blackcatClientHandler.sendMessage(req);
    }

    private static BlackcatClientHandler init() {
        try {
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new BlackcatClientInitializer());

            Channel ch = b.connect(HOST, BlackcatConfig.PORT).sync().channel();

            return ch.pipeline().get(BlackcatClientHandler.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
