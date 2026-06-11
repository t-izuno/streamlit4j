package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class FormWidgetsTest {

    @Test
    void formWrapsBodyAndCarriesKey() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.form("k1", () -> St.title("inside")));
            RenderNode form = root.children().get(0);
            assertThat(form.kind()).isEqualTo("form");
            assertThat(form.id()).isEqualTo("k_k1");
            assertThat(form.props()).containsEntry("key", "k1");
            assertThat(form.children().get(0).kind()).isEqualTo("title");
        }
    }

    @Test
    void formRestoresPriorSuppressedFlagAfterBody() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            AtomicBoolean innerSuppressed = new AtomicBoolean();
            runner.render(session, () -> St.form("outer", () -> {
                innerSuppressed.set(io.streamlit4j.core.runtime.RenderContext.current().isFormSuppressed());
            }));
            assertThat(innerSuppressed).isTrue();
        }
    }

    @Test
    void formSubmitButtonReturnsFalseInitially() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            runner.render(session, () -> St.form("f", () -> holder[0] = St.formSubmitButton("Go")));
            assertThat(holder[0]).isFalse();
        }
    }

    @Test
    void formSubmitButtonReturnsTrueWhenStoredAndResetsFlag() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            String[] idHolder = new String[1];
            RenderNode root = runner.render(session, () -> St.form("f", () -> {
                idHolder[0] = io.streamlit4j.core.runtime.WidgetIds.generate("form_submit_button", null, "Go");
                holder[0] = St.formSubmitButton("Go");
            }));
            assertThat(holder[0]).isFalse();
            String submitId = root.children().get(0).children().get(0).id();
            session.updateWidget(submitId, true);
            runner.render(session, () -> St.form("f", () -> holder[0] = St.formSubmitButton("Go")));
            assertThat(holder[0]).isTrue();
            assertThat(session.state().get(submitId)).isEqualTo(false);
        }
    }
}
