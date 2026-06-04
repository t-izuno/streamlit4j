package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.ComponentRegistry;

public final class ComponentRegistryAccess {

    private static volatile ComponentRegistry registry = new InMemoryComponentRegistry();

    private ComponentRegistryAccess() {}

    public static ComponentRegistry registry() {
        return registry;
    }

    public static void use(ComponentRegistry replacement) {
        registry = replacement;
    }
}
