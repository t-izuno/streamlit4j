package io.streamlit4j.core.port;

@FunctionalInterface
public interface EntrypointSource {

    Runnable next();
}
