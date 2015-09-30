package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgRsp;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BlackcatClientHandler
        extends SimpleChannelInboundHandler<BlackcatMsgRsp> {
    // Stateful properties
    private volatile Channel channel;

    public BlackcatClientHandler() {
        super(false);
    }

    public void sendMessage(BlackcatMsgReq req) {
        channel.writeAndFlush(req);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatMsgRsp rsp
    ) throws Exception {
        System.out.println(rsp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}