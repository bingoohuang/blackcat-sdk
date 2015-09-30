package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgRsp;
import com.google.common.base.Optional;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BlackcatServerHandler extends SimpleChannelInboundHandler<BlackcatMsgReq> {
    private final BlackcatMsgHandler blackcatMsgHandler;

    public BlackcatServerHandler(BlackcatMsgHandler blackcatMsgHandler) {
        this.blackcatMsgHandler = blackcatMsgHandler;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatMsgReq req) throws Exception {
        Optional<BlackcatMsgRsp> rsp = blackcatMsgHandler.handle(req);
        if (rsp.isPresent()) ctx.write(rsp.get());
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