package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SignalsTest {

    @Test
    void rerunRequestedCarriesFixedMessage() {
        RerunRequested signal = new RerunRequested();
        assertThat(signal.getMessage()).isEqualTo("rerun");
    }

    @Test
    void stopRequestedCarriesFixedMessage() {
        StopRequested signal = new StopRequested();
        assertThat(signal.getMessage()).isEqualTo("stop");
    }
}
