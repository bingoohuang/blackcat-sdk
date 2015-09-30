package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgRsp;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

public class BlackcatClientInitializer
        extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx = BlackcatConfig.configureSslForClient();

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc(),
                    BlackcatClient.HOST, BlackcatConfig.PORT));
        }

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(BlackcatMsgRsp.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());

        p.addLast(new BlackcatClientHandler());
    }
}