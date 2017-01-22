package com.github.bingoohuang.blackcat.sdk;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
 */
public interface BlackcatClientConfig {
    String getHostAndPort();

    int getReconnectDelay();
}
