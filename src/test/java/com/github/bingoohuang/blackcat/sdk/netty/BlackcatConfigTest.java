package com.github.bingoohuang.blackcat.sdk.netty;

import org.junit.Test;

import static com.github.bingoohuang.blackcat.sdk.netty.BlackcatConfig.getHostAndPort;
import static com.google.common.net.HostAndPort.fromString;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
 */
public class BlackcatConfigTest {
    @Test
    public void test() {
        assertThat(getHostAndPort()).isEqualTo(fromString("192.168.1.10:6667"));
    }
}
