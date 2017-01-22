package com.github.bingoohuang.blackcat.sdk.netty;

import com.github.bingoohuang.blackcat.sdk.BlackcatClientConfig;
import com.google.auto.service.AutoService;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
 */
@AutoService(BlackcatClientConfig.class)
public class MyBlackcatConfig implements BlackcatClientConfig {
    @Override public String getHostAndPort() {
        return "192.168.1.10:6667";
    }

    @Override public int getReconnectDelay() {
        return 15;
    }
}
