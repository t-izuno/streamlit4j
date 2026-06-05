package io.streamlit4j.core.api;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.RenderContext;
import io.streamlit4j.core.runtime.WidgetIds;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Package-private helpers shared by the widget facades under
 * {@code io.streamlit4j.core.api}. All emitters and session-state readers
 * funnel through here so per-category classes (TextWidgets, InputWidgets, ...)
 * stay focused on translating arguments into render-node props.
 */
final class WidgetSupport {

    private WidgetSupport() {}

    static void emit(String kind, String id, Map<String, Object> props) {
        RenderContext.current().addNode(new RenderNode(kind, id, props, List.of()));
    }

    static String widgetId(String kind, Object... discriminator) {
        return WidgetIds.generate(kind, null, discriminator);
    }

    static <T> T readStored(String id, Class<T> type, T fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (type.isInstance(stored)) {
            return type.cast(stored);
        }
        return fallback;
    }

    static int readStoredInt(String id, int fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (stored instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    static double readStoredDouble(String id, double fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (stored instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    static Map<String, Object> ordered(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    static void wrapContainer(String kind, String id, Map<String, Object> props, Runnable body) {
        RenderContext ctx = RenderContext.current();
        ctx.pushFrame();
        body.run();
        List<RenderNode> children = ctx.popFrame();
        ctx.addNode(new RenderNode(kind, id, props, children));
    }
}
