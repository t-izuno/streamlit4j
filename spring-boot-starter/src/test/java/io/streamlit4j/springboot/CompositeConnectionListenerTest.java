package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.port.EntrypointSource;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class CompositeConnectionListenerTest {

    @Test
    void singleListenerIsInvokedOnBothLifecycleCallbacks() {
        new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(Streamlit4jAutoConfiguration.class))
                .withUserConfiguration(SingleListenerConfig.class).run(context -> {
                    Streamlit4jWebSocketHandler handler = context.getBean(Streamlit4jWebSocketHandler.class);
                    SingleListenerConfig.Recorder recorder = context.getBean(SingleListenerConfig.Recorder.class);

                    WebSocketSessionWithAttributes ws = new WebSocketSessionWithAttributes();
                    handler.afterConnectionEstablished(ws);
                    assertThat(recorder.events).containsExactly("established");
                    handler.afterConnectionClosed(ws, org.springframework.web.socket.CloseStatus.NORMAL);
                    assertThat(recorder.events).containsExactly("established", "closed");
                });
    }

    @Test
    void multipleListenersFanOutOnEachCallback() {
        new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(Streamlit4jAutoConfiguration.class))
                .withUserConfiguration(MultiListenerConfig.class).run(context -> {
                    Streamlit4jWebSocketHandler handler = context.getBean(Streamlit4jWebSocketHandler.class);
                    MultiListenerConfig.Recorder recorder = context.getBean(MultiListenerConfig.Recorder.class);

                    WebSocketSessionWithAttributes ws = new WebSocketSessionWithAttributes();
                    handler.afterConnectionEstablished(ws);
                    handler.afterConnectionClosed(ws, org.springframework.web.socket.CloseStatus.NORMAL);
                    assertThat(recorder.established).isEqualTo(2);
                    assertThat(recorder.closed).isEqualTo(2);
                });
    }

    @Configuration
    static class SingleListenerConfig {
        @Bean
        Recorder recorder() {
            return new Recorder();
        }

        @Bean
        Streamlit4jConnectionListener listener(Recorder rec) {
            return new Streamlit4jConnectionListener() {
                @Override
                public void onConnectionEstablished(org.springframework.web.socket.WebSocketSession s, String id) {
                    rec.events.add("established");
                }

                @Override
                public void onConnectionClosed(org.springframework.web.socket.WebSocketSession s, String id) {
                    rec.events.add("closed");
                }
            };
        }

        @Bean
        EntrypointSource entrypoint() {
            return () -> () -> {
            };
        }

        static final class Recorder {
            final List<String> events = new ArrayList<>();
        }
    }

    @Configuration
    static class MultiListenerConfig {
        @Bean
        Recorder recorder() {
            return new Recorder();
        }

        @Bean
        Streamlit4jConnectionListener a(Recorder rec) {
            return tally(rec);
        }

        @Bean
        Streamlit4jConnectionListener b(Recorder rec) {
            return tally(rec);
        }

        @Bean
        EntrypointSource entrypoint() {
            return () -> () -> {
            };
        }

        static Streamlit4jConnectionListener tally(Recorder rec) {
            return new Streamlit4jConnectionListener() {
                @Override
                public void onConnectionEstablished(org.springframework.web.socket.WebSocketSession s, String id) {
                    rec.established++;
                }

                @Override
                public void onConnectionClosed(org.springframework.web.socket.WebSocketSession s, String id) {
                    rec.closed++;
                }
            };
        }

        static final class Recorder {
            int established;
            int closed;
        }
    }
}
