package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.TimeUnit;

@Slf4j
public class BlackcatClientHandler extends SimpleChannelInboundHandler<BlackcatRsp> {
    private final BlackcatNettyClient blackcatNettyClient;
    long startTime = -1;

    public BlackcatClientHandler(BlackcatNettyClient blackcatNettyClient) {
        super(false);
        this.blackcatNettyClient = blackcatNettyClient;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BlackcatRsp rsp) throws Exception {
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
        println("Sleeping for: " + BlackcatConfig.getReconnectDelay() + 's');

        loop.schedule(new Runnable() {
            @Override
            public void run() {
                println("Reconnecting to: " + BlackcatConfig.getHostAndPort());
                blackcatNettyClient.connect();
            }
        }, BlackcatConfig.getReconnectDelay(), TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        println("Disconnected from: " + ctx.channel().remoteAddress());
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
            val seconds = (System.currentTimeMillis() - startTime) / 1000;
            log.info("[UPTIME: {}] {}", seconds, msg);
        }
    }
}