package io.streamlit4j.examples.spring.showcase;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.ShowcaseDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the comprehensive streamlit4j Showcase demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit}).
 */
@SpringBootApplication
public class SpringBootShowcaseApp {

    /** Creates the application bean. */
    public SpringBootShowcaseApp() {}

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootShowcaseApp.class, args);
    }

    /**
     * Supplies the {@link ShowcaseDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> ShowcaseDemo::run;
    }
}
