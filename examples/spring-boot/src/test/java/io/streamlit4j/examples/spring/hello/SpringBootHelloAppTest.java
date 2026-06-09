package io.streamlit4j.examples.spring.hello;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.springboot.Streamlit4jWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringBootHelloApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootHelloAppTest {

    @Autowired
    private Streamlit4jApplication application;

    @Autowired
    private Streamlit4jWebSocketHandler webSocketHandler;

    @Autowired
    private EntrypointSource entrypointSource;

    @Test
    void contextLoadsWithAllStreamlit4jBeans() {
        assertThat(application).isNotNull();
        assertThat(webSocketHandler).isNotNull();
        assertThat(entrypointSource).isNotNull();
    }

    @Test
    void usesApplicationProvidedEntrypoint() {
        Runnable next = entrypointSource.next();
        assertThat(next).isNotNull();
    }
}
