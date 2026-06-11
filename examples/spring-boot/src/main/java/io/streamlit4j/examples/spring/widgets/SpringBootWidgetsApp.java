package io.streamlit4j.examples.spring.widgets;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.WidgetsDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Widgets gallery at {@code ${streamlit4j.base-path}} (default
 * {@code /streamlit}).
 */
@SpringBootApplication
public class SpringBootWidgetsApp {

    /** Creates the application bean. */
    public SpringBootWidgetsApp() {
    }

    /**
     * Boots the Spring application.
     *
     * @param args
     *            command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootWidgetsApp.class, args);
    }

    /**
     * Supplies the {@link WidgetsDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> WidgetsDemo::run;
    }
}
