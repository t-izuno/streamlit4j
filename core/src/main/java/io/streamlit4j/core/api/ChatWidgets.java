package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.readStored;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;
import static io.streamlit4j.core.api.WidgetSupport.wrapContainer;

import io.streamlit4j.core.runtime.RenderContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/** LLM chat-oriented primitives. */
final class ChatWidgets {

    private static final String KIND_CHAT_INPUT = "chat_input";
    private static final String KIND_CHAT_MESSAGE = "chat_message";
    private static final String KIND_CHAT_STREAM = "chat_stream";
    private static final String KIND_CHAT_CONTROLS = "chat_controls";
    private static final String KIND_CHAT_CONTAINER = "chat_container";
    private static final String KIND_TOOL_RESULT = "tool_result";

    private ChatWidgets() {
    }

    static void chatMessage(String role, String content) {
        emit(KIND_CHAT_MESSAGE, widgetId(KIND_CHAT_MESSAGE, role, content), ordered("role", role, "content", content));
    }

    static void chatMessage(String role, Runnable body) {
        wrapContainer(KIND_CHAT_MESSAGE, widgetId(KIND_CHAT_MESSAGE, role), ordered("role", role), body);
    }

    static String chatInput(String label) {
        String id = widgetId(KIND_CHAT_INPUT, label);
        String value = readStored(id, String.class, null);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("label", label);
        if (value != null) {
            props.put("value", value);
        }
        emit(KIND_CHAT_INPUT, id, props);
        return value;
    }

    static ChatAction chatControls() {
        String id = widgetId(KIND_CHAT_CONTROLS);
        Object stored = RenderContext.current().sessionState().remove(id);
        emit(KIND_CHAT_CONTROLS, id, ordered("actions", List.of("stop", "retry", "edit_regenerate")));
        if (!(stored instanceof Map<?, ?> raw)) {
            return null;
        }
        Object action = raw.get("action");
        if (!(action instanceof String actionName)) {
            return null;
        }
        String editedValue = raw.get("value") instanceof String s ? s : null;
        return new ChatAction(ChatAction.Type.fromWireName(actionName), editedValue);
    }

    static void chatContainer(Runnable body) {
        wrapContainer(KIND_CHAT_CONTAINER, widgetId(KIND_CHAT_CONTAINER), Map.of(), body);
    }

    static void toolResult(String title, String status, Runnable body) {
        wrapContainer(KIND_TOOL_RESULT, widgetId(KIND_TOOL_RESULT, title, status),
                ordered("title", title, "status", status), body);
    }

    static void writeStream(Iterable<String> tokens) {
        List<String> materialized = new ArrayList<>();
        for (String token : tokens) {
            materialized.add(token);
        }
        emit(KIND_CHAT_STREAM, widgetId(KIND_CHAT_STREAM, materialized), ordered("tokens", List.copyOf(materialized)));
    }

    static void writeStream(Stream<String> tokens) {
        writeStream(tokens.toList());
    }

    static void writeStream(Flow.Publisher<String> tokens) {
        PublisherCollector collector = new PublisherCollector();
        tokens.subscribe(collector);
        writeStream(collector.awaitTokens());
    }

    private static final class PublisherCollector implements Flow.Subscriber<String> {
        private final List<String> collected = new ArrayList<>();
        private final CountDownLatch completed = new CountDownLatch(1);
        private final AtomicReference<Throwable> error = new AtomicReference<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            collected.add(item);
        }

        @Override
        public void onError(Throwable throwable) {
            error.set(throwable);
            completed.countDown();
        }

        @Override
        public void onComplete() {
            completed.countDown();
        }

        List<String> awaitTokens() {
            try {
                completed.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while reading chat stream tokens", e);
            }
            Throwable thrown = error.get();
            if (thrown != null) {
                throw new IllegalStateException("Chat stream publisher failed", thrown);
            }
            return List.copyOf(collected);
        }
    }
}
