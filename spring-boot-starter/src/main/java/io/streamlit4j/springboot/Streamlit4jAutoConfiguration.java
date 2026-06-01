package io.streamlit4j.springboot;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import jakarta.servlet.http.HttpSessionListener;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Auto-configuration entrypoint for the streamlit4j Spring Boot starter.
 *
 * <p>Mounts the WebSocket endpoint at {@code ${streamlit4j.base-path}/ws} when the host
 * application is a servlet web environment and Spring WebSocket is on the classpath.
 * Optional inner configurations integrate with Spring Security and Servlet HTTP sessions
 * (compatible with Spring Session deployments). Static resource serving is added in TASK-096.
 */
@AutoConfiguration
@ConditionalOnClass({Bootstrap.class, WebSocketHandler.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(Streamlit4jProperties.class)
public class Streamlit4jAutoConfiguration {

    /** Normalizes a configured base path into a leading-slash, no-trailing-slash form ({@code ""} for root). */
    static String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank() || basePath.equals("/")) {
            return "";
        }
        String trimmed = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntrypointSource streamlit4jEntrypointSource() {
        return () -> () -> {};
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public Streamlit4jApplication streamlit4jApplication(EntrypointSource entrypoints) {
        return Bootstrap.standalone(entrypoints);
    }

    @Bean
    @ConditionalOnMissingBean
    public Streamlit4jWebSocketHandler streamlit4jWebSocketHandler(
            Streamlit4jApplication application, ObjectProvider<Streamlit4jConnectionListener> listeners) {
        Streamlit4jConnectionListener listener = composite(listeners);
        return new Streamlit4jWebSocketHandler(application, listener);
    }

    private static Streamlit4jConnectionListener composite(ObjectProvider<Streamlit4jConnectionListener> listeners) {
        List<Streamlit4jConnectionListener> ordered = listeners.orderedStream().toList();
        if (ordered.isEmpty()) {
            return Streamlit4jConnectionListener.NO_OP;
        }
        if (ordered.size() == 1) {
            return ordered.get(0);
        }
        return new Streamlit4jConnectionListener() {
            @Override
            public void onConnectionEstablished(
                    org.springframework.web.socket.WebSocketSession session, String streamlit4jSessionId) {
                for (Streamlit4jConnectionListener listener : ordered) {
                    listener.onConnectionEstablished(session, streamlit4jSessionId);
                }
            }

            @Override
            public void onConnectionClosed(
                    org.springframework.web.socket.WebSocketSession session, String streamlit4jSessionId) {
                for (Streamlit4jConnectionListener listener : ordered) {
                    listener.onConnectionClosed(session, streamlit4jSessionId);
                }
            }
        };
    }

    /**
     * Delegates authentication to the host application's Spring Security configuration
     * by copying the current {@link org.springframework.security.core.Authentication}
     * into WebSocket handshake attributes.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    static class SecurityIntegration {

        @Bean
        @ConditionalOnMissingBean
        public Streamlit4jPrincipalHandshakeInterceptor streamlit4jPrincipalHandshakeInterceptor() {
            return new Streamlit4jPrincipalHandshakeInterceptor();
        }
    }

    /**
     * Binds streamlit4j internal sessions to the hosting HTTP session and terminates them
     * when the HTTP session is destroyed. Works for both vanilla servlet sessions and
     * Spring Session deployments, since both surface as Servlet {@link jakarta.servlet.http.HttpSession}.
     */
    @Configuration(proxyBeanMethods = false)
    static class HttpSessionIntegration {

        @Bean
        @ConditionalOnMissingBean
        public Streamlit4jHttpSessionRegistry streamlit4jHttpSessionRegistry() {
            return new Streamlit4jHttpSessionRegistry();
        }

        @Bean
        @ConditionalOnMissingBean
        public Streamlit4jHttpSessionHandshakeInterceptor streamlit4jHttpSessionHandshakeInterceptor() {
            return new Streamlit4jHttpSessionHandshakeInterceptor();
        }

        @Bean
        public Streamlit4jHttpSessionBinder streamlit4jHttpSessionBinder(Streamlit4jHttpSessionRegistry registry) {
            return new Streamlit4jHttpSessionBinder(registry);
        }

        @Bean
        public ServletListenerRegistrationBean<HttpSessionListener> streamlit4jHttpSessionListenerRegistration(
                Streamlit4jHttpSessionRegistry registry, Streamlit4jApplication application) {
            return new ServletListenerRegistrationBean<>(new Streamlit4jHttpSessionListener(registry, application));
        }
    }

    /**
     * Serves the bundled streamlit4j frontend SPA at {@code ${streamlit4j.base-path}/**}.
     * Assets live in {@code META-INF/resources/streamlit4j/} on the classpath
     * (contributed by the {@code streamlit4j-frontend-assets} jar). Skipped when the
     * base path is empty or root to avoid clobbering Spring's default static handlers.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(WebMvcConfigurer.class)
    static class ResourceRegistration implements WebMvcConfigurer {

        static final String ASSET_LOCATION = "classpath:/META-INF/resources/streamlit4j/";

        private final Streamlit4jProperties properties;

        ResourceRegistration(Streamlit4jProperties properties) {
            this.properties = properties;
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            String basePath = normalizeBasePath(properties.getBasePath());
            if (basePath.isEmpty()) {
                return;
            }
            registry.addResourceHandler(basePath + "/**").addResourceLocations(ASSET_LOCATION);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSocket
    static class WebSocketRegistration implements WebSocketConfigurer {

        private final Streamlit4jWebSocketHandler handler;
        private final Streamlit4jProperties properties;
        private final ObjectProvider<HandshakeInterceptor> interceptors;

        WebSocketRegistration(
                Streamlit4jWebSocketHandler handler,
                Streamlit4jProperties properties,
                ObjectProvider<HandshakeInterceptor> interceptors) {
            this.handler = handler;
            this.properties = properties;
            this.interceptors = interceptors;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            WebSocketHandlerRegistration registration =
                    registry.addHandler(handler, normalizeBasePath(properties.getBasePath()) + "/ws");
            List<HandshakeInterceptor> all = interceptors.orderedStream().toList();
            if (!all.isEmpty()) {
                registration.addInterceptors(all.toArray(HandshakeInterceptor[]::new));
            }
        }
    }
}
