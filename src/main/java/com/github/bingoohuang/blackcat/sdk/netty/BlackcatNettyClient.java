package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.Setter;
import lombok.val;

import static com.github.bingoohuang.blackcat.sdk.netty.BlackcatConfig.getHostAndPort;

public final class BlackcatNettyClient {
    @Setter volatile Channel channel;
    EventBus eventBus = new EventBus();

    private void initSocketChannel(SocketChannel ch) {
        val p = ch.pipeline();
        val ctx = BlackcatConfig.configureSslForClient();
        if (ctx != null) {
            val hp = getHostAndPort();
            p.addLast(ctx.newHandler(ch.alloc(), hp.getHost(), hp.getPort()));
        }

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(BlackcatRsp.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new BlackcatClientHandler(this));
    }

    public void connect() {
        val hp = getHostAndPort();
        new Bootstrap()
                .group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(hp.getHost(), hp.getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        initSocketChannel(ch);
                    }
                }).connect();
    }

    public void send(BlackcatReq req) {
        if (channel != null) channel.writeAndFlush(req);
    }

    public void post(Object o) {
        eventBus.post(o);
    }

    public void register(Object o) {
        eventBus.register(o);
    }
}
