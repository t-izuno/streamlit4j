package io.streamlit4j.examples.spring;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.Hello;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Hello demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit}).
 *
 * <p>Run with {@code mvn -pl examples spring-boot:run} once the spring-boot-maven-plugin
 * is wired, or use any standard {@code main}-based launch.
 */
@SpringBootApplication
public class SpringBootHelloApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootHelloApp.class, args);
    }

    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> Hello::run;
    }
}
