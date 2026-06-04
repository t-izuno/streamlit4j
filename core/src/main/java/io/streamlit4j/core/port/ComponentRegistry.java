package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.CustomComponent;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry of in-process custom components — i.e. components whose React renderer
 * ships in the frontend bundle distributed with streamlit4j (as opposed to
 * third-party {@code iframe}-isolated components handled by TASK-101).
 *
 * <p>Registration is the canonical way for an app to declare which component
 * names the bundled frontend is expected to resolve to a built-in renderer.
 * Downstream tasks (bundle pipeline, frontend bridge) consult this registry to
 * surface the available components to the client.
 */
public interface ComponentRegistry {

    /**
     * Registers a component under its {@link CustomComponent#name() name}. Re-registering
     * the same name overwrites the previous entry.
     */
    void register(CustomComponent<?> component);

    /** Returns the registered component for the given name, if any. */
    Optional<CustomComponent<?>> find(String name);

    /** Returns an unmodifiable snapshot of all registered components. */
    Collection<CustomComponent<?>> all();

    /** Returns the count of currently registered components. */
    int size();
}
