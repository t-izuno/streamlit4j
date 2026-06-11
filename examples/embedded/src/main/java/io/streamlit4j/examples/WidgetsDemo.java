package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Demonstrates the input widget surface: text / number / select / radio / checkbox / button / slider / date / time /
 * color picker. Runnable as {@code java -cp <classpath> io.streamlit4j.examples.WidgetsDemo [port]}.
 */
public final class WidgetsDemo {

    private static final int DEFAULT_PORT = 8501;

    private WidgetsDemo() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Widgets gallery");
        St.markdown("A quick tour of the input widgets available in **streamlit4j**.");

        St.header("Text and number");
        String name = St.textInput("Your name", "Ada");
        double amount = St.numberInput("Amount", 1000.0);
        St.write("Hello, " + name + " — selected amount: " + amount);

        St.header("Choice");
        String role = St.selectbox("Role", List.of("Engineer", "Designer", "Manager"));
        String env = St.radio("Environment", List.of("dev", "staging", "prod"));
        boolean notify = St.checkbox("Notify on completion", true);
        St.write("Role=" + role + " env=" + env + " notify=" + notify);

        St.header("Pickers");
        LocalDate when = St.dateInput("When", LocalDate.now());
        LocalTime at = St.timeInput("At", LocalTime.of(9, 0));
        String accent = St.colorPicker("Accent color", "#4f46e5");
        St.write("when=" + when + " at=" + at + " accent=" + accent);

        St.header("Slider");
        int year = St.slider("Year", 2020, 2030, 2026);
        St.metric("Selected year", year);

        if (St.button("Run")) {
            St.toast("Submitted at " + LocalTime.now());
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
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> WidgetsDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
