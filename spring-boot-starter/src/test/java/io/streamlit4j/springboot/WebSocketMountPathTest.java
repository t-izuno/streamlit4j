package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

class WebSocketMountPathTest {

    @ParameterizedTest
    @CsvSource({
        "/streamlit, /streamlit/ws",
        "/, /ws",
        "'', /ws",
        "/apps/demo, /apps/demo/ws",
        "/apps/demo/, /apps/demo/ws",
        "streamlit, /streamlit/ws"
    })
    void registersHandlerAtNormalizedPath(String basePath, String expectedPath) {
        Streamlit4jProperties properties = new Streamlit4jProperties();
        properties.setBasePath(basePath);
        Streamlit4jAutoConfiguration.WebSocketRegistration registration =
                new Streamlit4jAutoConfiguration.WebSocketRegistration(null, properties, emptyProvider());

        CapturingRegistry registry = new CapturingRegistry();
        registration.registerWebSocketHandlers(registry);

        assertThat(registry.paths).containsExactly(expectedPath);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<HandshakeInterceptor> emptyProvider() {
        return mock(ObjectProvider.class, invocation -> {
            if (invocation.getMethod().getName().equals("orderedStream")) {
                return Stream.empty();
            }
            return null;
        });
    }

    private static final class CapturingRegistry implements WebSocketHandlerRegistry {
        private final List<String> paths = new ArrayList<>();

        @Override
        public WebSocketHandlerRegistration addHandler(WebSocketHandler handler, String... pathPatterns) {
            for (String path : pathPatterns) {
                paths.add(path);
            }
            return new NoopRegistration();
        }
    }

    private static final class NoopRegistration implements WebSocketHandlerRegistration {

        @Override
        public WebSocketHandlerRegistration addHandler(WebSocketHandler handler, String... pathPatterns) {
            return this;
        }

        @Override
        public WebSocketHandlerRegistration setHandshakeHandler(
                org.springframework.web.socket.server.HandshakeHandler handshakeHandler) {
            return this;
        }

        @Override
        public WebSocketHandlerRegistration addInterceptors(
                org.springframework.web.socket.server.HandshakeInterceptor... interceptors) {
            return this;
        }

        @Override
        public WebSocketHandlerRegistration setAllowedOrigins(String... origins) {
            return this;
        }

        @Override
        public WebSocketHandlerRegistration setAllowedOriginPatterns(String... originPatterns) {
            return this;
        }

        @Override
        public org.springframework.web.socket.config.annotation.SockJsServiceRegistration withSockJS() {
            throw new UnsupportedOperationException("not needed in tests");
        }
    }
}
