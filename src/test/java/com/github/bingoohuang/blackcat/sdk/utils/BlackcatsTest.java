package com.github.bingoohuang.blackcat.sdk.utils;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BlackcatsTest {
    @Test
    public void testSplitLinesWoComments() {
        List<String> strings = Blackcats.splitLinesWoComments("aaa\nbbb", "#");
        assertThat(strings).hasSize(2).contains("aaa", "bbb");

        strings = Blackcats.splitLinesWoComments("aaa\nbbb #这里是注释", "#");
        assertThat(strings).hasSize(2).contains("aaa", "bbb");

        strings = Blackcats.splitLinesWoComments("aaa\n#bbb", "#");
        assertThat(strings).hasSize(1).contains("aaa");

        strings = Blackcats.splitLinesWoComments("\n\naaa\n#bbb", "#");
        assertThat(strings).hasSize(1).contains("aaa");
    }
}
