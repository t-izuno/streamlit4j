package io.streamlit4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Top-level metadata accessor for the streamlit4j core module.
 * <p>
 * Currently exposes the artifact version read at build time from {@code streamlit4j/version.properties} (Maven
 * resource-filtered with {@code ${project.version}}). This keeps the version a single source of truth in
 * {@code pom.xml} and avoids drift between code and POM.
 */
public final class Streamlit4j {

    private static final String VERSION = loadVersion();

    private Streamlit4j() {
    }

    /**
     * Returns the artifact version, e.g. {@code "0.1.0-SNAPSHOT"}.
     *
     * @return version string read from {@code streamlit4j/version.properties}
     */
    public static String version() {
        return VERSION;
    }

    private static String loadVersion() {
        try (InputStream in = Streamlit4j.class.getResourceAsStream("/streamlit4j/version.properties")) {
            if (in == null) {
                return "unknown";
            }
            Properties props = new Properties();
            props.load(in);
            String value = props.getProperty("version");
            return value == null || value.isBlank() ? "unknown" : value;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
