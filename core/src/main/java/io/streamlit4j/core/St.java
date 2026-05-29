package io.streamlit4j.core;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.RenderContext;
import io.streamlit4j.core.runtime.WidgetIds;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class St {

    private St() {}

    public static void title(String text) {
        RenderContext ctx = RenderContext.current();
        String id = WidgetIds.generate("title", null, text);
        ctx.addNode(new RenderNode("title", id, Map.of("text", text), List.of()));
    }

    public static void markdown(String body) {
        RenderContext ctx = RenderContext.current();
        String id = WidgetIds.generate("markdown", null, body);
        ctx.addNode(new RenderNode("markdown", id, Map.of("body", body), List.of()));
    }

    public static void write(Object value) {
        RenderContext ctx = RenderContext.current();
        String id = WidgetIds.generate("write", null, value);
        ctx.addNode(new RenderNode("write", id, Map.of("value", String.valueOf(value)), List.of()));
    }

    public static int slider(String label, int min, int max, int defaultValue) {
        RenderContext ctx = RenderContext.current();
        String id = WidgetIds.generate("slider", null, label, min, max);
        int currentValue = readIntFromState(ctx.sessionState().get(id), defaultValue);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("label", label);
        props.put("min", min);
        props.put("max", max);
        props.put("value", currentValue);
        ctx.addNode(new RenderNode("slider", id, props, List.of()));
        return currentValue;
    }

    private static int readIntFromState(Object stored, int fallback) {
        if (stored instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }
}
