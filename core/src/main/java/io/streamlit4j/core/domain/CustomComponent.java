package io.streamlit4j.core.domain;

import java.util.Objects;

/**
 * Type-safe declaration of a custom component invocable from {@code St.component(...)}.
 * <p>
 * Declares the component's logical {@code name} (used by the frontend to look up the registered renderer) and the
 * {@code resultType} of values the component sends back through widget events. The declaration is intentionally
 * minimal: argument schemas and serialization details are handled by {@code ComponentCodec}.
 *
 * @param <R>
 *            the type of value the component yields back to the script
 * @param name
 *            logical component name used by the frontend registry
 * @param resultType
 *            declared Java type of values returned through widget events
 */
public record CustomComponent<R>(String name, Class<R> resultType) {

    /**
     * Validates that {@code name} is non-null, non-blank and {@code resultType} is non-null. Throws
     * {@link IllegalArgumentException} when {@code name} is blank.
     */
    public CustomComponent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(resultType, "resultType");
        if (name.isBlank()) {
            throw new IllegalArgumentException("component name must not be blank");
        }
    }

    /**
     * Convenience for components that have no return value (display-only).
     *
     * @param name
     *            logical component name
     *
     * @return a {@code CustomComponent} declaring {@link Void} as the result type
     */
    public static CustomComponent<Void> ofVoid(String name) {
        return new CustomComponent<>(name, Void.class);
    }
}
