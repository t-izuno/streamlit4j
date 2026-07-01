package io.streamlit4j.examples;

import io.streamlit4j.core.api.ChatAction;
import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.server.Streamlit4jServer;
import java.util.List;
import java.util.Map;

/**
 * Fake LLM chat demo with deterministic token output. It exercises streamed tokens, incomplete Markdown fragments,
 * stop, retry, edit-regenerate, rich chat content, and tool-result rendering without calling an external API.
 */
public final class FakeLlmChatDemo {

    private static final int DEFAULT_PORT = 8501;
    private static final String PROMPT_KEY = "fake_llm.prompt";
    private static final String LAST_INPUT_KEY = "fake_llm.last_chat_input";
    private static final String STOPPED_KEY = "fake_llm.stopped";
    private static final String REVISION_KEY = "fake_llm.revision";

    private FakeLlmChatDemo() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        SessionState state = St.state();
        St.title("Fake LLM chat");
        St.markdown("A deterministic local LLM stand-in for validating chat UI behavior.");

        String submitted = St.chatInput("Ask Fake LLM");
        if (submitted != null && !submitted.isBlank()
                && !submitted.equals(state.get(LAST_INPUT_KEY, String.class).orElse(null))) {
            String prompt = submitted.trim();
            acceptPrompt(state, prompt);
            state.put(LAST_INPUT_KEY, prompt);
        }

        ChatAction[] action = new ChatAction[1];
        St.chatContainer(() -> {
            String prompt = state.get(PROMPT_KEY, String.class).orElse(null);
            if (prompt == null) {
                St.chatMessage("assistant", "Ask a question to generate a deterministic response.");
            } else {
                renderConversation(state, prompt);
            }
            action[0] = St.chatControls();
        });

        if (action[0] != null) {
            applyAction(state, action[0]);
            St.rerun();
        }
    }

    private static void renderConversation(SessionState state, String prompt) {
        int revision = state.get(REVISION_KEY, Integer.class).orElse(0);
        boolean stopped = state.get(STOPPED_KEY, Boolean.class).orElse(false);
        St.chatMessage("user", prompt);
        St.chatMessage("assistant", () -> {
            St.writeStream(fakeTokens(prompt, revision, stopped));
            St.toolResult("Fake retrieval", stopped ? "cancelled" : "success",
                    () -> St.table(List.of(Map.of("query", prompt, "revision", revision))));
        });
    }

    private static List<String> fakeTokens(String prompt, int revision, boolean stopped) {
        List<String> full = List.of("Answer for `", prompt, "` (retry ", String.valueOf(revision), ")\n\n", "```java\n",
                "System.out.println(\"", prompt.replace("\"", "\\\""), "\");\n", "```\n\n", "| item | value |\n",
                "| --- | --- |\n", "| prompt | ", prompt, " |\n\n", "$$\n", "score = ",
                String.valueOf(prompt.length() + revision), "\n$$");
        if (!stopped) {
            return full;
        }
        return List.of(full.get(0), full.get(1), "`\n\n_generation stopped_");
    }

    private static void applyAction(SessionState state, ChatAction action) {
        switch (action.type()) {
            case STOP -> state.put(STOPPED_KEY, true);
            case RETRY -> {
                state.put(STOPPED_KEY, false);
                state.put(REVISION_KEY, state.get(REVISION_KEY, Integer.class).orElse(0) + 1);
            }
            case EDIT_REGENERATE -> {
                if (action.value() != null && !action.value().isBlank()) {
                    acceptPrompt(state, action.value().trim());
                }
            }
        }
    }

    private static void acceptPrompt(SessionState state, String prompt) {
        state.put(PROMPT_KEY, prompt);
        state.put(STOPPED_KEY, false);
        state.put(REVISION_KEY, 0);
    }

    /**
     * Boots an embedded server that serves this demo on the given port.
     *
     * @param args
     *            optional single positional argument: the listen port (default {@value #DEFAULT_PORT})
     *
     * @throws Exception
     *             when the server fails to start
     */
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> FakeLlmChatDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
