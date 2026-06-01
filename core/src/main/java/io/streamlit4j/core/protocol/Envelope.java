package io.streamlit4j.core.protocol;

public sealed interface Envelope permits SessionInit, RenderDelta, WidgetEvent, ErrorMessage, FileUpload, ReloadNotice {

    int PROTOCOL_VERSION = 1;

    int v();

    String type();

    String sessionId();
}
