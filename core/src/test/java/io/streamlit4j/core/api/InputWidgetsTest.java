package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InputWidgetsTest {

    @Test
    void textInputReturnsStoredString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.textInput("name", "def"));
            session.updateWidget(root.children().get(0).id(), "Alice");
            runner.render(session, () -> holder[0] = St.textInput("name", "def"));
            assertThat(holder[0]).isEqualTo("Alice");
        }
    }

    @Test
    void numberInputReturnsStoredDouble() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            double[] holder = new double[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.numberInput("n", 1.0));
            session.updateWidget(root.children().get(0).id(), 3.5);
            runner.render(session, () -> holder[0] = St.numberInput("n", 1.0));
            assertThat(holder[0]).isEqualTo(3.5);
        }
    }

    @Test
    void textAreaReturnsStoredString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.textArea("bio", "init"));
            session.updateWidget(root.children().get(0).id(), "edited");
            runner.render(session, () -> holder[0] = St.textArea("bio", "init"));
            assertThat(holder[0]).isEqualTo("edited");
        }
    }

    @Test
    void selectboxReturnsFirstOptionWhenNoneStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            runner.render(session, () -> holder[0] = St.selectbox("r", List.of("A", "B")));
            assertThat(holder[0]).isEqualTo("A");
        }
    }

    @Test
    void selectboxReturnsStoredValueWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.selectbox("r", List.of("A", "B")));
            session.updateWidget(root.children().get(0).id(), "B");
            runner.render(session, () -> holder[0] = St.selectbox("r", List.of("A", "B")));
            assertThat(holder[0]).isEqualTo("B");
        }
    }

    @Test
    void multiselectReturnsStoredListWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            @SuppressWarnings("unchecked")
            List<String>[] holder = new List[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.multiselect("t", List.of("a", "b", "c")));
            session.updateWidget(root.children().get(0).id(), List.of("a", "c"));
            runner.render(session, () -> holder[0] = St.multiselect("t", List.of("a", "b", "c")));
            assertThat(holder[0]).containsExactly("a", "c");
        }
    }

    @Test
    void multiselectReturnsEmptyWhenNoneStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            @SuppressWarnings("unchecked")
            List<String>[] holder = new List[1];
            runner.render(session, () -> holder[0] = St.multiselect("t", List.of("a", "b")));
            assertThat(holder[0]).isEmpty();
        }
    }

    @Test
    void checkboxWithDefaultReturnsDefaultThenStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.checkbox("a", true));
            assertThat(holder[0]).isTrue();
            session.updateWidget(root.children().get(0).id(), false);
            runner.render(session, () -> holder[0] = St.checkbox("a", true));
            assertThat(holder[0]).isFalse();
        }
    }

    @Test
    void radioReturnsFirstOptionWhenNoneStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            runner.render(session, () -> holder[0] = St.radio("m", List.of("light", "dark")));
            assertThat(holder[0]).isEqualTo("light");
        }
    }

    @Test
    void radioReturnsStoredOptionWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.radio("m", List.of("light", "dark")));
            session.updateWidget(root.children().get(0).id(), "dark");
            runner.render(session, () -> holder[0] = St.radio("m", List.of("light", "dark")));
            assertThat(holder[0]).isEqualTo("dark");
        }
    }

    @Test
    void buttonReturnsTrueWhenClickedThenResetsStoredFlag() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.button("Go"));
            String id = root.children().get(0).id();
            assertThat(holder[0]).isFalse();
            session.updateWidget(id, true);
            runner.render(session, () -> holder[0] = St.button("Go"));
            assertThat(holder[0]).isTrue();
            assertThat(session.state().get(id)).isEqualTo(false);
        }
    }

    @Test
    void dateInputUsesStoredStringWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            LocalDate[] holder = new LocalDate[1];
            LocalDate base = LocalDate.of(2026, 1, 1);
            RenderNode root = runner.render(session, () -> holder[0] = St.dateInput("d", base));
            session.updateWidget(root.children().get(0).id(), "2030-06-15");
            runner.render(session, () -> holder[0] = St.dateInput("d", base));
            assertThat(holder[0]).isEqualTo(LocalDate.of(2030, 6, 15));
        }
    }

    @Test
    void dateInputUsesDefaultWhenStoredIsNotString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            LocalDate[] holder = new LocalDate[1];
            LocalDate base = LocalDate.of(2026, 1, 1);
            RenderNode root = runner.render(session, () -> holder[0] = St.dateInput("d", base));
            session.updateWidget(root.children().get(0).id(), 12345); // Number, not String.
            runner.render(session, () -> holder[0] = St.dateInput("d", base));
            assertThat(holder[0]).isEqualTo(base);
        }
    }

    @Test
    void timeInputUsesStoredStringWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            LocalTime[] holder = new LocalTime[1];
            LocalTime base = LocalTime.NOON;
            RenderNode root = runner.render(session, () -> holder[0] = St.timeInput("t", base));
            session.updateWidget(root.children().get(0).id(), "06:30");
            runner.render(session, () -> holder[0] = St.timeInput("t", base));
            assertThat(holder[0]).isEqualTo(LocalTime.of(6, 30));
        }
    }

    @Test
    void timeInputUsesDefaultWhenStoredIsNotString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            LocalTime[] holder = new LocalTime[1];
            LocalTime base = LocalTime.NOON;
            RenderNode root = runner.render(session, () -> holder[0] = St.timeInput("t", base));
            session.updateWidget(root.children().get(0).id(), 12345);
            runner.render(session, () -> holder[0] = St.timeInput("t", base));
            assertThat(holder[0]).isEqualTo(base);
        }
    }

    @Test
    void colorPickerReturnsStoredString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.colorPicker("c", "#fff"));
            session.updateWidget(root.children().get(0).id(), "#abcdef");
            runner.render(session, () -> holder[0] = St.colorPicker("c", "#fff"));
            assertThat(holder[0]).isEqualTo("#abcdef");
        }
    }

    @Test
    void selectSliderReturnsStoredOrDefault() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session,
                    () -> holder[0] = St.selectSlider("size", List.of("S", "M", "L"), "M"));
            assertThat(holder[0]).isEqualTo("M");
            session.updateWidget(root.children().get(0).id(), "L");
            runner.render(session, () -> holder[0] = St.selectSlider("size", List.of("S", "M", "L"), "M"));
            assertThat(holder[0]).isEqualTo("L");
        }
    }

    @Test
    void checkboxNoDefaultReturnsFalse() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            runner.render(session, () -> holder[0] = St.checkbox("a"));
            assertThat(holder[0]).isFalse();
        }
    }
}
