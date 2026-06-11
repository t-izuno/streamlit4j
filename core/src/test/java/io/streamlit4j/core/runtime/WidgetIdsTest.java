package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WidgetIdsTest {

    @Test
    void explicitKeyTakesPrecedenceAndUsesKPrefix() {
        String id = WidgetIds.generate("slider", "myKey", 1, 2);
        assertThat(id).isEqualTo("k_myKey");
    }

    @Test
    void blankExplicitKeyFallsBackToDerivedId() {
        String id = WidgetIds.generate("slider", "", 1);
        assertThat(id).startsWith("w_");
    }

    @Test
    void nullExplicitKeyDerivesFromCallSite() {
        String id = WidgetIds.generate("slider", null, "arg");
        assertThat(id).startsWith("w_");
    }

    @Test
    void sameKindAndKeyProduceSameId() {
        String a = WidgetIds.generate("slider", "k1");
        String b = WidgetIds.generate("slider", "k1");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentExplicitKeysProduceDifferentIds() {
        assertThat(WidgetIds.generate("x", "a")).isNotEqualTo(WidgetIds.generate("x", "b"));
    }

    @Test
    void nullKindThrows() {
        assertThatThrownBy(() -> WidgetIds.generate(null, "k")).isInstanceOf(NullPointerException.class);
    }

    @Test
    void differentDiscriminatorsProduceDifferentDerivedIds() {
        String a = WidgetIds.generate("x", null, 1);
        String b = WidgetIds.generate("x", null, 2);
        // Same call site, different discriminator → different derived id.
        assertThat(a).isNotEqualTo(b);
    }
}
