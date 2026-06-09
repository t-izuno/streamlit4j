package io.streamlit4j.examples.spring.chat;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.ChatDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Chat demo at
 * {@code ${streamlit4j.base-path}} (default {@code /streamlit4j}).
 */
@SpringBootApplication
public class SpringBootChatApp {

    /** Creates the application bean. */
    public SpringBootChatApp() {}

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootChatApp.class, args);
    }

    /**
     * Supplies the {@link ChatDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> ChatDemo::run;
    }
}
