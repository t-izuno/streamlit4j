package io.streamlit4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Streamlit4jTest {

    @Test
    void versionIsZeroDotOne() {
        assertThat(Streamlit4j.version()).startsWith("0.1");
    }
}
