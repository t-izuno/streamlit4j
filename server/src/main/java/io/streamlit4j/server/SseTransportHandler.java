package io.streamlit4j.server;

import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.ErrorMessage;
import io.streamlit4j.core.protocol.FileUpload;
import io.streamlit4j.core.protocol.Patch;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.protocol.RenderDelta;
import io.streamlit4j.core.protocol.SessionInit;
import io.streamlit4j.core.protocol.WidgetEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * SSE + HTTP POST transport for the streamlit4j protocol.
 */
final class SseTransportHandler extends Handler.Abstract {

    private static final String EVENTS_PATH = "/events";

    private final StartSession startSession;
    private final ProcessWidgetEvent processWidgetEvent;
    private final ConnectionRegistry connections;

    SseTransportHandler(StartSession startSession, ProcessWidgetEvent processWidgetEvent,
            ConnectionRegistry connections) {
        this.startSession = startSession;
        this.processWidgetEvent = processWidgetEvent;
        this.connections = connections;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String path = Request.getPathInContext(request);
        if (!EVENTS_PATH.equals(path)) {
            return false;
        }
        if ("GET".equals(request.getMethod())) {
            openEventStream(request, response, callback);
            return true;
        }
        if ("POST".equals(request.getMethod())) {
            handleClientEnvelope(request, response, callback);
            return true;
        }
        Response.writeError(request, response, callback, HttpStatus.METHOD_NOT_ALLOWED_405);
        return true;
    }

    private void openEventStream(Request request, Response response, Callback callback) {
        response.setStatus(HttpStatus.OK_200);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/event-stream; charset=utf-8");
        response.getHeaders().put(HttpHeader.CACHE_CONTROL, "no-cache");
        response.getHeaders().put(HttpHeader.CONNECTION, "keep-alive");
        try {
            StartSession.Result result = startSession.execute();
            SseConnection connection = new SseConnection(response);
            connections.register(result.sessionId(), connection);
            Request.addCompletionListener(request, failure -> connections.remove(result.sessionId()));
            connection.deliver(SessionInit.of(result.sessionId(), result.root()));
        } catch (Exception e) {
            response.write(true, ByteBuffer.wrap(sseData(ErrorMessage.of(null, e.getMessage(), stackTrace(e)))),
                    callback);
        }
    }

    private void handleClientEnvelope(Request request, Response response, Callback callback) throws Exception {
        try {
            String body = new String(Request.asInputStream(request).readAllBytes(), StandardCharsets.UTF_8);
            Envelope incoming = Codec.decode(body);
            if (incoming instanceof WidgetEvent event) {
                ProcessWidgetEvent.Result result = processWidgetEvent.execute(event.sessionId(), event.widgetId(),
                        ProtocolEndpoint.unwrap(event.value()));
                connections.deliver(event.sessionId(),
                        RenderDelta.of(event.sessionId(), result.seq(), result.patches()));
            } else if (incoming instanceof FileUpload upload) {
                byte[] bytes = Base64.getDecoder().decode(upload.contentBase64());
                ProtocolEndpoint.UploadedFile file = new ProtocolEndpoint.UploadedFile(upload.filename(),
                        upload.mimeType(), bytes);
                ProcessWidgetEvent.Result result = processWidgetEvent.execute(upload.sessionId(), upload.widgetId(),
                        file);
                connections.deliver(upload.sessionId(),
                        RenderDelta.of(upload.sessionId(), result.seq(), result.patches()));
            }
            response.setStatus(HttpStatus.ACCEPTED_202);
            response.write(true, ByteBuffer.allocate(0), callback);
        } catch (Exception e) {
            Response.writeError(request, response, callback, HttpStatus.BAD_REQUEST_400, e.getMessage());
        }
    }

    private static byte[] sseData(Envelope envelope) {
        return ("event: message\n" + "data: " + Codec.encode(envelope) + "\n\n").getBytes(StandardCharsets.UTF_8);
    }

    private static List<Envelope> expandStreamingEnvelope(Envelope envelope) {
        if (envelope instanceof SessionInit init) {
            int maxTokens = maxStreamTokenCount(init.root());
            if (maxTokens <= 1) {
                return List.of(envelope);
            }
            List<Envelope> expanded = new ArrayList<>();
            expanded.add(SessionInit.of(init.sessionId(), limitStreamTokens(init.root(), 1)));
            for (int tokenLimit = 2; tokenLimit <= maxTokens; tokenLimit++) {
                expanded.add(RenderDelta.of(init.sessionId(), init.v(),
                        List.of(Patch.replace("/", limitStreamTokens(init.root(), tokenLimit)))));
            }
            return expanded;
        }
        if (envelope instanceof RenderDelta delta) {
            int maxTokens = maxStreamTokenCount(delta.patches());
            if (maxTokens <= 1) {
                return List.of(envelope);
            }
            List<Envelope> expanded = new ArrayList<>();
            for (int tokenLimit = 1; tokenLimit <= maxTokens; tokenLimit++) {
                expanded.add(
                        RenderDelta.of(delta.sessionId(), delta.seq(), limitStreamTokens(delta.patches(), tokenLimit)));
            }
            return expanded;
        }
        return List.of(envelope);
    }

    private static int maxStreamTokenCount(List<Patch> patches) {
        int max = 0;
        for (Patch patch : patches) {
            max = Math.max(max, maxStreamTokenCount(patch.node()));
        }
        return max;
    }

    private static int maxStreamTokenCount(RenderNode node) {
        if (node == null) {
            return 0;
        }
        int max = 0;
        if ("chat_stream".equals(node.kind())) {
            Object tokens = node.props().get("tokens");
            if (tokens instanceof List<?> tokenList) {
                max = tokenList.size();
            }
        }
        for (RenderNode child : node.children()) {
            max = Math.max(max, maxStreamTokenCount(child));
        }
        return max;
    }

    private static List<Patch> limitStreamTokens(List<Patch> patches, int tokenLimit) {
        List<Patch> limited = new ArrayList<>();
        for (Patch patch : patches) {
            limited.add(new Patch(patch.op(), patch.path(), limitStreamTokens(patch.node(), tokenLimit)));
        }
        return limited;
    }

    private static RenderNode limitStreamTokens(RenderNode node, int tokenLimit) {
        if (node == null) {
            return null;
        }
        Map<String, Object> props = node.props();
        if ("chat_stream".equals(node.kind())) {
            Object tokens = props.get("tokens");
            if (tokens instanceof List<?> tokenList && tokenList.size() > tokenLimit) {
                props = new LinkedHashMap<>(props);
                props.put("tokens", List.copyOf(tokenList.subList(0, tokenLimit)));
            }
        }
        List<RenderNode> children = new ArrayList<>();
        for (RenderNode child : node.children()) {
            children.add(limitStreamTokens(child, tokenLimit));
        }
        return new RenderNode(node.kind(), node.id(), props, children);
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static final class SseConnection implements ProtocolConnection {
        private final Response response;

        SseConnection(Response response) {
            this.response = response;
        }

        @Override
        public synchronized void deliver(Envelope envelope) {
            for (Envelope frame : expandStreamingEnvelope(envelope)) {
                response.write(false, ByteBuffer.wrap(sseData(frame)), Callback.NOOP);
            }
        }
    }
}
