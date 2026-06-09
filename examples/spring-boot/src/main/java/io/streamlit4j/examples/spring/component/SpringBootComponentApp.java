package io.streamlit4j.examples.spring.component;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.ComponentDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j custom Component demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit}).
 */
@SpringBootApplication
public class SpringBootComponentApp {

    /** Creates the application bean. */
    public SpringBootComponentApp() {}

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootComponentApp.class, args);
    }

    /**
     * Supplies the {@link ComponentDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> ComponentDemo::run;
    }
}
