package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.domain.CustomComponent;
import org.junit.jupiter.api.Test;

class InMemoryComponentRegistryTest {

    @Test
    void registerStoresComponentByName() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        CustomComponent<String> chart = new CustomComponent<>("my-chart", String.class);

        registry.register(chart);

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.find("my-chart")).contains(chart);
    }

    @Test
    void findReturnsEmptyForUnknownName() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        assertThat(registry.find("missing")).isEmpty();
    }

    @Test
    void reRegisterOverwritesPreviousEntry() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        CustomComponent<String> first = new CustomComponent<>("picker", String.class);
        CustomComponent<Integer> second = new CustomComponent<>("picker", Integer.class);

        registry.register(first);
        registry.register(second);

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.find("picker")).contains(second);
    }

    @Test
    void allReturnsSnapshotOfRegisteredComponents() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        CustomComponent<String> a = new CustomComponent<>("a", String.class);
        CustomComponent<Integer> b = new CustomComponent<>("b", Integer.class);

        registry.register(a);
        registry.register(b);

        assertThat(registry.all()).containsExactlyInAnyOrder(a, b);
    }

    @Test
    void allIsUnmodifiable() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        registry.register(new CustomComponent<>("a", String.class));

        assertThatThrownBy(() -> registry.all().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void registerRejectsNull() {
        InMemoryComponentRegistry registry = new InMemoryComponentRegistry();
        assertThatThrownBy(() -> registry.register(null)).isInstanceOf(NullPointerException.class);
    }
}
