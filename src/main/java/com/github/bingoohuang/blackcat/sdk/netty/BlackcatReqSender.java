package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg;

public interface BlackcatReqSender {
    void send(BlackcatMsg.BlackcatReq req);
}
