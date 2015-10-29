package com.github.bingoohuang.blackcat.sdk;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import io.netty.channel.ChannelHandlerContext;

public interface BlackcatMsgHandler {
    void handle(BlackcatReq req, ChannelHandlerContext ctx);
}
