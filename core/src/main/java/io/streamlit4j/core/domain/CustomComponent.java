package io.streamlit4j.core.domain;

import java.util.Objects;

/**
 * Type-safe declaration of a custom component invocable from {@code St.component(...)}.
 *
 * <p>Declares the component's logical {@code name} (used by the frontend to look up the
 * registered renderer) and the {@code resultType} of values the component sends back
 * through widget events. The declaration is intentionally minimal: argument schemas and
 * serialization details are handled by TASK-098 / TASK-099 / TASK-104.
 *
 * @param <R> the type of value the component yields back to the script
 */
public record CustomComponent<R>(String name, Class<R> resultType) {

    public CustomComponent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(resultType, "resultType");
        if (name.isBlank()) {
            throw new IllegalArgumentException("component name must not be blank");
        }
    }

    /** Convenience for components that have no return value (display-only). */
    public static CustomComponent<Void> ofVoid(String name) {
        return new CustomComponent<>(name, Void.class);
    }
}
