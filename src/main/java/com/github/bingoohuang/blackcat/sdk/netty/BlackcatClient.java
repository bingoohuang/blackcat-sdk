package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

import static com.github.bingoohuang.blackcat.sdk.netty.BlackcatConfig.HOST;
import static com.github.bingoohuang.blackcat.sdk.netty.BlackcatConfig.PORT;

public final class BlackcatClient {
    EventLoopGroup eventLoop = new NioEventLoopGroup(1);
    volatile Channel channel;
    BlackcatClientHandler clientHandler;
    EventBus eventBus = new EventBus();

    public void send(BlackcatReq req) {
        if (channel != null) channel.writeAndFlush(req);
    }

    private final SslContext sslCtx = BlackcatConfig.configureSslForClient();

    private Bootstrap configureBootstrap() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoop);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            clientHandler = new BlackcatClientHandler(BlackcatClient.this);

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null)
                        p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));

                    p.addLast(new ProtobufVarint32FrameDecoder());
                    p.addLast(new ProtobufDecoder(BlackcatRsp.getDefaultInstance()));

                    p.addLast(new ProtobufVarint32LengthFieldPrepender());
                    p.addLast(new ProtobufEncoder());


                    p.addLast(clientHandler);
                }
            });
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            throw Throwables.propagate(e);
        }
    }

    public void connect() {
        configureBootstrap().connect();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void post(Object o) {
        eventBus.post(o);
    }

    public void register(Object o) {
        eventBus.register(o);
    }
}
