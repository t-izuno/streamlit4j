package io.streamlit4j.core.runtime;

import java.lang.StackWalker.StackFrame;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Generates stable widget identifiers. Uses an explicit key when supplied; otherwise
 * derives a hash from the calling user frame plus the supplied discriminator so
 * repeated render runs produce the same id for the same widget call site.
 */
public final class WidgetIds {

    private static final StackWalker WALKER = StackWalker.getInstance();

    private WidgetIds() {}

    /**
     * Builds a widget id for the current call site.
     *
     * @param kind widget kind tag (e.g. {@code "slider"})
     * @param explicitKey user-supplied key (takes precedence when non-empty)
     * @param discriminator additional arguments that make the id specific to the call
     * @return widget id ({@code "k_*"} for explicit keys, {@code "w_*"} for derived)
     */
    public static String generate(String kind, String explicitKey, Object... discriminator) {
        Objects.requireNonNull(kind, "kind");
        if (explicitKey != null && !explicitKey.isEmpty()) {
            return "k_" + explicitKey;
        }
        String location = userFrame()
                .map(f -> f.getClassName() + ":" + f.getMethodName() + ":" + f.getLineNumber())
                .orElse("unknown");
        int hash = Objects.hash(kind, location, Arrays.deepHashCode(discriminator));
        return "w_" + Integer.toUnsignedString(hash, 16);
    }

    private static Optional<StackFrame> userFrame() {
        return WALKER.walk(stream -> stream.dropWhile(f -> f.getClassName().startsWith("io.streamlit4j.core."))
                .findFirst());
    }
}
