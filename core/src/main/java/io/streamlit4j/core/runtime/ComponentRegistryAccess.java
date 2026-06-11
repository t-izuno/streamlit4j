package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.ComponentRegistry;

/**
 * Process-wide access point for the active {@link ComponentRegistry}. Allows host applications to swap implementations
 * without changing the {@code St.registerComponent} / {@code St.component} APIs.
 */
public final class ComponentRegistryAccess {

    private static volatile ComponentRegistry registry = new InMemoryComponentRegistry();

    private ComponentRegistryAccess() {
    }

    /**
     * Returns the active component registry.
     *
     * @return current registry
     */
    public static ComponentRegistry registry() {
        return registry;
    }

    /**
     * Replaces the active component registry.
     *
     * @param replacement
     *            new registry implementation
     */
    public static void use(ComponentRegistry replacement) {
        registry = replacement;
    }
}
