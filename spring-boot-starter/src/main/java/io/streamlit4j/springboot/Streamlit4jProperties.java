package io.streamlit4j.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the streamlit4j Spring Boot starter.
 *
 * <p>Bound under the {@code streamlit4j} prefix in {@code application.properties} /
 * {@code application.yml}. Property semantics will be expanded by follow-up tasks
 * (TASK-093 onward); v1 starts with the mount path.
 */
@ConfigurationProperties(prefix = "streamlit4j")
public class Streamlit4jProperties {

    private String basePath = "/streamlit";

    /** Creates a properties instance populated with defaults. */
    public Streamlit4jProperties() {}

    /**
     * Returns the mount path used for the streamlit4j WebSocket and assets.
     *
     * @return base path (defaults to {@code "/streamlit"})
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Sets the mount path.
     *
     * @param basePath new mount path
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
