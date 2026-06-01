package io.streamlit4j.springboot;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration entrypoint for the streamlit4j Spring Boot starter.
 *
 * <p>This class only declares the configuration boundary; actual bean wiring for
 * session management, mount path, security delegation, and resource serving is
 * provided by follow-up tasks (TASK-093 to TASK-096).
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.streamlit4j.core.bootstrap.Bootstrap")
@EnableConfigurationProperties(Streamlit4jProperties.class)
public class Streamlit4jAutoConfiguration {}
