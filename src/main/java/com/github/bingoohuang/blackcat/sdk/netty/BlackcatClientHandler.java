package com.github.bingoohuang.blackcat.sdk.netty;


import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BlackcatClientHandler
        extends SimpleChannelInboundHandler<BlackcatRsp> {
    Logger log = LoggerFactory.getLogger(BlackcatClientHandler.class);

    private final BlackcatNettyClient blackcatNettyClient;
    long startTime = -1;

    public BlackcatClientHandler(BlackcatNettyClient blackcatNettyClient) {
        super(false);
        this.blackcatNettyClient = blackcatNettyClient;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatRsp rsp
    ) throws Exception {
        System.out.println(rsp);

        Object o = Blackcats.parseRspBody(rsp);
        blackcatNettyClient.post(o);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (startTime < 0) startTime = System.currentTimeMillis();
        println("Connected to: " + ctx.channel().remoteAddress());

        Channel channel = ctx.channel();
        blackcatNettyClient.setChannel(channel);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        blackcatNettyClient.setChannel(null);
        reconnect(ctx.channel().eventLoop());
    }

    public void reconnect(final EventLoop loop) {
        println("Sleeping for: " + BlackcatConfig.RECONNECT_DELAY + 's');

        loop.schedule(new Runnable() {
            @Override
            public void run() {
                println("Reconnecting to: " + BlackcatConfig.HOST + ':' + BlackcatConfig.PORT);
                blackcatNettyClient.connect();
            }
        }, BlackcatConfig.RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Disconnected from: " + ctx.channel().remoteAddress());
        blackcatNettyClient.setChannel(null);

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    void println(String msg) {
        if (startTime < 0) {
            log.info("[SERVER IS DOWN] {}", msg);
        } else {
            long seconds = (System.currentTimeMillis() - startTime) / 1000;
            log.info("[UPTIME: {}] {}", seconds, msg);
        }
    }
}