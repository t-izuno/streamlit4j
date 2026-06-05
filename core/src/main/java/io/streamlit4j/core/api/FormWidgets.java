package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.readStored;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.RenderContext;
import java.util.List;
import java.util.Map;

/** Form container and submit button. */
final class FormWidgets {

    private FormWidgets() {}

    static void form(String key, Runnable body) {
        RenderContext ctx = RenderContext.current();
        String id = "k_" + key;
        ctx.pushFrame();
        boolean prev = ctx.isFormSuppressed();
        ctx.setFormSuppressed(true);
        try {
            body.run();
        } finally {
            ctx.setFormSuppressed(prev);
        }
        List<RenderNode> children = ctx.popFrame();
        ctx.addNode(new RenderNode("form", id, Map.of("key", key), children));
    }

    static boolean formSubmitButton(String label) {
        String id = widgetId("form_submit_button", label);
        boolean clicked = readStored(id, Boolean.class, false);
        if (clicked) {
            RenderContext.current().sessionState().put(id, false);
        }
        emit("form_submit_button", id, ordered("label", label));
        return clicked;
    }
}
