package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.server.Streamlit4jServer;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal chat demo: a bot that echoes back whatever you type, prefixed with {@code "echo: "}. Demonstrates
 * session-state-backed message history, the form widget for an Enter-to-submit input, and the rerun-on-event loop.
 * Runnable as {@code java -cp <classpath> io.streamlit4j.examples.ChatDemo [port]}.
 */
public final class ChatDemo {

    private static final int DEFAULT_PORT = 8501;
    private static final String STATE_KEY = "chat_messages";

    private ChatDemo() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    @SuppressWarnings("unchecked")
    public static void run() {
        St.title("Echo bot");
        St.markdown("Type a message and the bot will echo it back, prefixed with `echo:`. "
                + "The conversation history is held in `St.state()`, so it survives reruns "
                + "but resets when you reload the page (a fresh session).");

        SessionState state = St.state();
        List<Message> stored = (List<Message>) state.get(STATE_KEY, List.class).orElse(null);
        if (stored == null) {
            stored = new ArrayList<>();
            state.put(STATE_KEY, stored);
        }
        final List<Message> messages = stored;

        St.form("chat_input", () -> {
            String text = St.textInput("Your message", "");
            boolean sent = St.formSubmitButton("Send");
            if (sent && !text.isBlank()) {
                String trimmed = text.trim();
                messages.add(new Message("user", trimmed));
                messages.add(new Message("bot", "echo: " + trimmed));
            }
        });

        St.divider();

        if (messages.isEmpty()) {
            St.markdown("_No messages yet. Send something above._");
        } else {
            for (Message m : messages) {
                String speaker = "user".equals(m.role()) ? "**You**" : "**Bot**";
                St.markdown(speaker + " — " + m.text());
            }
        }

        if (!messages.isEmpty() && St.button("Clear conversation")) {
            messages.clear();
            St.rerun();
        }
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
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> ChatDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }

    /** One chat message, either from the user or the echo bot. */
    private record Message(String role, String text) {
    }
}
