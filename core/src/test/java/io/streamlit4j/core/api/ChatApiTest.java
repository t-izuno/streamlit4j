package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;

class ChatApiTest {

    @Test
    void chatMessageEmitsRoleAndContent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.chatMessage("assistant", "Hello"));

            RenderNode message = root.children().get(0);
            assertThat(message.kind()).isEqualTo("chat_message");
            assertThat(message.props()).containsEntry("role", "assistant").containsEntry("content", "Hello");
        }
    }

    @Test
    void chatInputReturnsStoredSubmittedText() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode firstRoot = runner.render(session, () -> St.chatInput("Ask"));
            String widgetId = firstRoot.children().get(0).id();
            session.updateWidget(widgetId, "What changed?");

            String[] captured = new String[1];
            RenderNode secondRoot = runner.render(session, () -> captured[0] = St.chatInput("Ask"));

            assertThat(captured[0]).isEqualTo("What changed?");
            assertThat(secondRoot.children().get(0).kind()).isEqualTo("chat_input");
            assertThat(secondRoot.children().get(0).props()).containsEntry("label", "Ask");
        }
    }

    @Test
    void writeStreamEmitsTokenList() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.writeStream(List.of("Hel", "lo")));

            RenderNode stream = root.children().get(0);
            assertThat(stream.kind()).isEqualTo("chat_stream");
            assertThat(stream.props()).containsEntry("tokens", List.of("Hel", "lo"));
        }
    }

    @Test
    void writeStreamConsumesPublisherTokens() {
        Flow.Publisher<String> publisher = subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    subscriber.onNext("Hel");
                    subscriber.onNext("lo");
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {
                }
            });
        };

        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.writeStream(publisher));

            RenderNode stream = root.children().get(0);
            assertThat(stream.kind()).isEqualTo("chat_stream");
            assertThat(stream.props()).containsEntry("tokens", List.of("Hel", "lo"));
        }
    }

    @Test
    void chatControlsReturnsOneShotAction() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode firstRoot = runner.render(session, St::chatControls);
            String widgetId = firstRoot.children().get(0).id();
            session.updateWidget(widgetId, Map.of("action", "edit_regenerate", "value", "Edited prompt"));

            ChatAction[] captured = new ChatAction[1];
            RenderNode secondRoot = runner.render(session, () -> captured[0] = St.chatControls());

            assertThat(captured[0]).isEqualTo(new ChatAction(ChatAction.Type.EDIT_REGENERATE, "Edited prompt"));
            assertThat(secondRoot.children().get(0).kind()).isEqualTo("chat_controls");
            assertThat(session.state()).doesNotContainKey(widgetId);
        }
    }

    @Test
    void chatContainerWrapsChatChildren() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.chatContainer(() -> St.chatMessage("assistant", "Hi")));

            RenderNode container = root.children().get(0);
            assertThat(container.kind()).isEqualTo("chat_container");
            assertThat(container.children()).hasSize(1);
            assertThat(container.children().get(0).kind()).isEqualTo("chat_message");
        }
    }

    @Test
    void chatMessageCanContainRichChildrenAndToolResult() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.chatMessage("assistant", () -> {
                St.code("System.out.println(1);", "java");
                St.fileUploader("Attach context");
                St.downloadButton("Download answer", "/download/a1");
                St.toolResult("Search", "success", () -> St.table(List.of(Map.of("name", "streamlit4j"))));
                St.component("answer-card", Map.of("title", "Generated panel"));
            }));

            RenderNode message = root.children().get(0);
            assertThat(message.kind()).isEqualTo("chat_message");
            assertThat(message.children()).extracting(RenderNode::kind).containsExactly("code", "file_uploader",
                    "download_button", "tool_result", "component");
            RenderNode toolResult = message.children().get(3);
            assertThat(toolResult.props()).containsEntry("title", "Search").containsEntry("status", "success");
            assertThat(toolResult.children()).extracting(RenderNode::kind).containsExactly("table");
            assertThat(message.children().get(4).props()).containsEntry("name", "answer-card");
        }
    }
}
