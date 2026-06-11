package io.streamlit4j.core.port;

/**
 * Port that supplies the script body to execute for each session run. Implementations resolve the entrypoint from a
 * {@code .java} source file, a Spring bean, or a hard-coded {@link Runnable}.
 */
@FunctionalInterface
public interface EntrypointSource {

    /**
     * Returns the next entrypoint to execute.
     *
     * @return the script body
     */
    Runnable next();
}
