package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

public class BlackcatClientHandler
        extends SimpleChannelInboundHandler<BlackcatRsp> {

    private final BlackcatClient blackcatClient;
    long startTime = -1;

    public BlackcatClientHandler(BlackcatClient blackcatClient) {
        super(false);
        this.blackcatClient = blackcatClient;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatRsp rsp
    ) throws Exception {
        System.out.println(rsp);

        Object o = Blackcats.parseRspBody(rsp);
        blackcatClient.post(o);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (startTime < 0) startTime = System.currentTimeMillis();
        println("Connected to: " + ctx.channel().remoteAddress());

        Channel channel = ctx.channel();
        blackcatClient.setChannel(channel);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        blackcatClient.setChannel(null);
        reconnect(ctx.channel().eventLoop());
    }

    public void reconnect(final EventLoop loop) {
        println("Sleeping for: " + BlackcatConfig.RECONNECT_DELAY + 's');

        loop.schedule(new Runnable() {
            @Override
            public void run() {
                println("Reconnecting to: " + BlackcatConfig.HOST + ':' + BlackcatConfig.PORT);
                blackcatClient.connect();
            }
        }, BlackcatConfig.RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Disconnected from: " + ctx.channel().remoteAddress());
        blackcatClient.setChannel(null);

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    void println(String msg) {
        if (startTime < 0) {
            System.err.format("[SERVER IS DOWN] %s%n", msg);
        } else {
            System.err.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - startTime) / 1000, msg);
        }
    }
}