package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import io.streamlit4j.core.domain.Page;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.RenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Multi-page navigation. */
final class PageNav {

    private PageNav() {
    }

    static void pages(List<Page> pages) {
        if (pages.isEmpty()) {
            return;
        }
        RenderContext ctx = RenderContext.current();
        Object stored = ctx.sessionState().get("__page__");
        String currentPath = stored instanceof String s ? s : pages.get(0).path();
        Page current = pages.stream().filter(p -> p.path().equals(currentPath)).findFirst().orElse(pages.get(0));
        List<Map<String, Object>> pageList = new ArrayList<>();
        for (Page p : pages) {
            pageList.add(Map.of("name", p.name(), "path", p.path()));
        }
        String id = widgetId("pages");
        ctx.addNode(new RenderNode("pages", id, ordered("pages", pageList, "current", current.path()), List.of()));
        current.body().run();
    }
}
