package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.Map;

/** Media playback widgets (image, audio, video). */
final class MediaWidgets {

    private MediaWidgets() {
    }

    static void image(String url) {
        emit("image", widgetId("image", url), Map.of("src", url));
    }

    static void audio(String url) {
        emit("audio", widgetId("audio", url), Map.of("src", url));
    }

    static void video(String url) {
        emit("video", widgetId("video", url), Map.of("src", url));
    }
}
