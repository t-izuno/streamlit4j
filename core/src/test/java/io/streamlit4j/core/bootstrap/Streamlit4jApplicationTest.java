package io.streamlit4j.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Streamlit4jApplicationTest {

    @Test
    void closeInvokesResourceCloser() throws Exception {
        boolean[] closed = new boolean[1];
        AutoCloseable closer = () -> closed[0] = true;
        Streamlit4jApplication app = new Streamlit4jApplication(null, null, null, null, null, closer);
        app.close();
        assertThat(closed[0]).isTrue();
    }

    @Test
    void metricsRecordExposesActiveCount() {
        Streamlit4jApplication.Metrics m = new Streamlit4jApplication.Metrics(7);
        assertThat(m.activeSessions()).isEqualTo(7);
    }
}
