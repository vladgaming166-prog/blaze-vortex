package io.github.blazevortex.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamesTest {

    @Test
    void fromUserInputStripsPathAndExtension() {
        assertThat(Names.fromUserInput("fire")).isEqualTo("fire");
        assertThat(Names.fromUserInput("Fire.PNG")).isEqualTo("fire");
        assertThat(Names.fromUserInput("emojis/my_logo.png")).isEqualTo("my_logo");
        assertThat(Names.fromUserInput("my-logo")).isEqualTo("my_logo");
    }
}
