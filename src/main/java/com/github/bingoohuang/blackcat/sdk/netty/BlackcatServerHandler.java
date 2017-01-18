package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlackcatServerHandler extends SimpleChannelInboundHandler<BlackcatReq> {
    private final BlackcatMsgHandler blackcatMsgHandler;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatReq req) throws Exception {
        blackcatMsgHandler.handle(req, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}