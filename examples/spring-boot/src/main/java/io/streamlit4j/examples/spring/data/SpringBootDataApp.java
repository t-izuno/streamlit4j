package io.streamlit4j.examples.spring.data;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.DataDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Data display demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit}).
 */
@SpringBootApplication
public class SpringBootDataApp {

    /** Creates the application bean. */
    public SpringBootDataApp() {}

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataApp.class, args);
    }

    /**
     * Supplies the {@link DataDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> DataDemo::run;
    }
}
