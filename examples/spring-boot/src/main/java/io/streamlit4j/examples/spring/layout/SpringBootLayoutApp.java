package io.streamlit4j.examples.spring.layout;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.LayoutDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Layout demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit}).
 */
@SpringBootApplication
public class SpringBootLayoutApp {

    /** Creates the application bean. */
    public SpringBootLayoutApp() {}

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootLayoutApp.class, args);
    }

    /**
     * Supplies the {@link LayoutDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> LayoutDemo::run;
    }
}
