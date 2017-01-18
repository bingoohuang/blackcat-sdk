package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.utils.QuietCloseable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

public final class BlackcatServer {
    private static final SslContext sslCtx = BlackcatConfig.configureSslForServer();

    @SneakyThrows
    public static void startup(final BlackcatMsgHandler blackcatMsgHandler) {
        val bossGroup = new NioEventLoopGroup(1);
        val workerGroup = new NioEventLoopGroup();
        @Cleanup val i = new QuietCloseable() {
            public void close() {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        };

        val b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.handler(new LoggingHandler(LogLevel.INFO));
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                if (sslCtx != null)
                    p.addLast(sslCtx.newHandler(ch.alloc()));

                p.addLast(new ProtobufVarint32FrameDecoder());
                p.addLast(new ProtobufDecoder(BlackcatReq.getDefaultInstance()));
                p.addLast(new ProtobufVarint32LengthFieldPrepender());
                p.addLast(new ProtobufEncoder());
                p.addLast(new BlackcatServerHandler(blackcatMsgHandler));
            }
        });

        Channel channel = b.bind(BlackcatConfig.PORT).sync().channel();
        channel.closeFuture().sync();
    }
}
