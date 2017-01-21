package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.utils.QuietCloseable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
public final class BlackcatServer {
    final BlackcatMsgHandler blackcatMsgHandler;

    @SneakyThrows
    public void startup() {
        val bossGroup = new NioEventLoopGroup(1);
        val workerGroup = new NioEventLoopGroup();
        @Cleanup val i = new QuietCloseable() {
            public void close() {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        };

        val b = createServerBootstrap(bossGroup, workerGroup);
        val channel = b.bind(BlackcatConfig.PORT).sync().channel();
        channel.closeFuture().sync();
    }

    private ServerBootstrap createServerBootstrap(
            final NioEventLoopGroup bossGroup,
            final NioEventLoopGroup workerGroup) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        initSocketChannel(ch);
                    }
                });
    }

    private void initSocketChannel(SocketChannel ch) {
        val p = ch.pipeline();
        val sslCtx = BlackcatConfig.configureSslForServer();
        if (sslCtx != null) p.addLast(sslCtx.newHandler(ch.alloc()));

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(BlackcatReq.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new BlackcatServerHandler(blackcatMsgHandler));
    }
}
