package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;
import static io.streamlit4j.core.api.WidgetSupport.wrapContainer;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.RenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/** Layout primitives (columns / container / expander / tabs / sidebar / empty). */
final class LayoutWidgets {

    private LayoutWidgets() {
    }

    static void columns(int count, IntConsumer body) {
        RenderContext ctx = RenderContext.current();
        String id = widgetId("columns", count);
        List<RenderNode> columnNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ctx.pushFrame();
            body.accept(i);
            columnNodes.add(new RenderNode("column", id + "_" + i, Map.of("index", i), ctx.popFrame()));
        }
        ctx.addNode(new RenderNode("columns", id, Map.of("count", count), columnNodes));
    }

    static void container(Runnable body) {
        wrapContainer("container", widgetId("container"), Map.of(), body);
    }

    static void expander(String label, Runnable body) {
        wrapContainer("expander", widgetId("expander", label), Map.of("label", label), body);
    }

    static void tabs(List<String> labels, IntConsumer body) {
        RenderContext ctx = RenderContext.current();
        String id = widgetId("tabs", labels);
        List<RenderNode> tabNodes = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            ctx.pushFrame();
            body.accept(i);
            tabNodes.add(new RenderNode("tab", id + "_" + i, Map.of("label", labels.get(i)), ctx.popFrame()));
        }
        ctx.addNode(new RenderNode("tabs", id, Map.of("labels", labels), tabNodes));
    }

    static void sidebar(Runnable body) {
        wrapContainer("sidebar", widgetId("sidebar"), Map.of(), body);
    }

    static void empty() {
        emit("empty", widgetId("empty"), Map.of());
    }
}
