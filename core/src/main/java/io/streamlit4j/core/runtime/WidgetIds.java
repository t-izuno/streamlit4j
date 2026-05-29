package io.streamlit4j.core.runtime;

import java.lang.StackWalker.StackFrame;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class WidgetIds {

    private static final StackWalker WALKER = StackWalker.getInstance();

    private WidgetIds() {}

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
