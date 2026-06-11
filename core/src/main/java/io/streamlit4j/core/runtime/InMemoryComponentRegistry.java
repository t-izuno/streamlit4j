package io.streamlit4j.core.runtime;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.port.ComponentRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory {@link ComponentRegistry} backed by a {@link ConcurrentMap}. Suitable for single-process deployments.
 */
public final class InMemoryComponentRegistry implements ComponentRegistry {

    private final ConcurrentMap<String, CustomComponent<?>> components = new ConcurrentHashMap<>();

    /** Creates an empty registry. */
    public InMemoryComponentRegistry() {
    }

    @Override
    public void register(CustomComponent<?> component) {
        Objects.requireNonNull(component, "component");
        components.put(component.name(), component);
    }

    @Override
    public Optional<CustomComponent<?>> find(String name) {
        return Optional.ofNullable(components.get(name));
    }

    @Override
    public Collection<CustomComponent<?>> all() {
        return Collections.unmodifiableCollection(components.values());
    }

    @Override
    public int size() {
        return components.size();
    }
}
