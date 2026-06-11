package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class Streamlit4jStartupBannerTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void doesNothingWhenContextIsNotWebServer() {
        Streamlit4jProperties props = new Streamlit4jProperties();
        Streamlit4jStartupBanner banner = new Streamlit4jStartupBanner(props);
        try (ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.refresh();
            banner.onReady(new ApplicationReadyEvent(new SpringApplication(), null, ctx, java.time.Duration.ZERO));
        }
        assertThat(captured.toString()).doesNotContain("streamlit4j is ready");
    }

    @Test
    void doesNothingWhenWebServerNull() {
        Streamlit4jProperties props = new Streamlit4jProperties();
        Streamlit4jStartupBanner banner = new Streamlit4jStartupBanner(props);
        ConfigurableApplicationContext ctx = new TestWebServerApplicationContext(null);
        banner.onReady(new ApplicationReadyEvent(new SpringApplication(), null, ctx, java.time.Duration.ZERO));
        assertThat(captured.toString()).doesNotContain("streamlit4j is ready");
    }

    @Test
    void prefixesBasePathFromProperties() {
        Streamlit4jProperties props = new Streamlit4jProperties();
        props.setBasePath("/apps/demo");
        Streamlit4jStartupBanner banner = new Streamlit4jStartupBanner(props);
        ConfigurableApplicationContext ctx = new TestWebServerApplicationContext(new StubWebServer(8080));
        banner.onReady(new ApplicationReadyEvent(new SpringApplication(), null, ctx, java.time.Duration.ZERO));
        String out = captured.toString();
        assertThat(out).contains("streamlit4j is ready").contains("http://localhost:8080/apps/demo")
                .contains("ws://localhost:8080/apps/demo/ws");
    }

    @Test
    void usesRootMountWhenBasePathEmpty() {
        Streamlit4jProperties props = new Streamlit4jProperties();
        props.setBasePath("");
        Streamlit4jStartupBanner banner = new Streamlit4jStartupBanner(props);
        ConfigurableApplicationContext ctx = new TestWebServerApplicationContext(new StubWebServer(8081));
        banner.onReady(new ApplicationReadyEvent(new SpringApplication(), null, ctx, java.time.Duration.ZERO));
        String out = captured.toString();
        assertThat(out).contains("http://localhost:8081/");
        assertThat(out).contains("ws://localhost:8081/ws");
    }

    private static final class StubWebServer implements WebServer {
        private final int port;

        StubWebServer(int port) {
            this.port = port;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public int getPort() {
            return port;
        }
    }

    /** Minimal {@link WebServerApplicationContext} test double for banner verification. */
    private static final class TestWebServerApplicationContext extends AnnotationConfigApplicationContext
            implements WebServerApplicationContext {
        private final WebServer webServer;

        TestWebServerApplicationContext(WebServer webServer) {
            super();
            this.webServer = webServer;
            refresh();
        }

        @Override
        public WebServer getWebServer() {
            return webServer;
        }

        @Override
        public String getServerNamespace() {
            return "";
        }
    }
}
