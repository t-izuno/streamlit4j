package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Streamlit4jAutoConfigurationHelpersTest {

    @Test
    void normalizeBasePathReturnsEmptyForNull() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath(null)).isEmpty();
    }

    @Test
    void normalizeBasePathReturnsEmptyForBlank() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath("   ")).isEmpty();
    }

    @Test
    void normalizeBasePathReturnsEmptyForRoot() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath("/")).isEmpty();
    }

    @Test
    void normalizeBasePathStripsTrailingSlash() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath("/apps/")).isEqualTo("/apps");
    }

    @Test
    void normalizeBasePathAddsLeadingSlash() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath("apps")).isEqualTo("/apps");
    }

    @Test
    void normalizeBasePathKeepsAlreadyNormalizedValue() {
        assertThat(Streamlit4jAutoConfiguration.normalizeBasePath("/apps/demo")).isEqualTo("/apps/demo");
    }

    @Test
    void propertiesDefaultsToStreamlit4j() {
        Streamlit4jProperties p = new Streamlit4jProperties();
        assertThat(p.getBasePath()).isEqualTo("/streamlit4j");
    }

    @Test
    void propertiesGetterReflectsSetter() {
        Streamlit4jProperties p = new Streamlit4jProperties();
        p.setBasePath("/x");
        assertThat(p.getBasePath()).isEqualTo("/x");
    }

    @Test
    void connectionListenerNoOpAcceptsBothLifecycleCallbacks() {
        WebSocketSessionWithAttributes session = new WebSocketSessionWithAttributes();
        Streamlit4jConnectionListener.NO_OP.onConnectionEstablished(session, "s");
        Streamlit4jConnectionListener.NO_OP.onConnectionClosed(session, "s");
    }
}
