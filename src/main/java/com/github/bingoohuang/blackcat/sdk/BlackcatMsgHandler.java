package com.github.bingoohuang.blackcat.sdk;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMsgRsp;
import com.google.common.base.Optional;

public interface BlackcatMsgHandler {
    Optional<BlackcatMsgRsp> handle(BlackcatMsgReq req);
}
