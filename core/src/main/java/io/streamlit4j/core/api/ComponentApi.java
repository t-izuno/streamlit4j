package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.protocol.ComponentCodec;
import io.streamlit4j.core.runtime.ComponentRegistryAccess;
import io.streamlit4j.core.runtime.RenderContext;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registration and invocation of in-process custom components. */
final class ComponentApi {

    private ComponentApi() {}

    static <R> CustomComponent<R> registerComponent(CustomComponent<R> spec) {
        ComponentRegistryAccess.registry().register(spec);
        return spec;
    }

    static <R> R component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue) {
        String id = widgetId("component", spec.name(), args);
        Object stored = RenderContext.current().sessionState().get(id);
        R value = ComponentCodec.coerce(stored, spec.resultType(), defaultValue);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", spec.name());
        props.put("args", args);
        if (value != null) {
            props.put("value", value);
        }
        emit("component", id, props);
        return value;
    }

    static void component(String name, Map<String, Object> args) {
        String id = widgetId("component", name, args);
        emit("component", id, ordered("name", name, "args", args));
    }
}
