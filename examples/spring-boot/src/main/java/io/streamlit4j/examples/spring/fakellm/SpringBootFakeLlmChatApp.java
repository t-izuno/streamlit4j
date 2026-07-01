package io.streamlit4j.examples.spring.fakellm;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.examples.FakeLlmChatDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot embedded sample that mounts the streamlit4j Fake LLM chat demo at {@code ${streamlit4j.base-path}}
 * (default {@code /streamlit4j}).
 */
@SpringBootApplication
public class SpringBootFakeLlmChatApp {

    /** Creates the application bean. */
    public SpringBootFakeLlmChatApp() {
    }

    /**
     * Boots the Spring application.
     *
     * @param args
     *            command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootFakeLlmChatApp.class, args);
    }

    /**
     * Supplies the {@link FakeLlmChatDemo} demo as the streamlit4j entrypoint.
     *
     * @return entrypoint source bean
     */
    @Bean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> FakeLlmChatDemo::run;
    }
}
