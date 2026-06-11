package io.streamlit4j.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageTest {

    @Test
    void recordExposesConstructorComponents() {
        Runnable body = () -> {
        };
        Page page = new Page("Home", "/home", body);
        assertThat(page.name()).isEqualTo("Home");
        assertThat(page.path()).isEqualTo("/home");
        assertThat(page.body()).isSameAs(body);
    }

    @Test
    void recordsWithSameComponentsAreEqual() {
        Runnable body = () -> {
        };
        Page a = new Page("Home", "/home", body);
        Page b = new Page("Home", "/home", body);
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
