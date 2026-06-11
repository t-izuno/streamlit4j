package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Page;
import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PageNavTest {

    @Test
    void emptyPagesIsNoOp() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.pages(List.of()));
            assertThat(root.children()).isEmpty();
        }
    }

    @Test
    void selectsFirstPageWhenNoneStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            AtomicReference<String> bodyMarker = new AtomicReference<>();
            Page home = new Page("Home", "/home", () -> bodyMarker.set("home"));
            Page about = new Page("About", "/about", () -> bodyMarker.set("about"));
            RenderNode root = runner.render(session, () -> St.pages(List.of(home, about)));
            RenderNode pagesNode = root.children().get(0);
            assertThat(pagesNode.kind()).isEqualTo("pages");
            assertThat(pagesNode.props()).containsEntry("current", "/home");
            assertThat(bodyMarker.get()).isEqualTo("home");
        }
    }

    @Test
    void usesStoredCurrentPathWhenPresent() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            session.state().put("__page__", "/about");
            AtomicReference<String> bodyMarker = new AtomicReference<>();
            Page home = new Page("Home", "/home", () -> bodyMarker.set("home"));
            Page about = new Page("About", "/about", () -> bodyMarker.set("about"));
            RenderNode root = runner.render(session, () -> St.pages(List.of(home, about)));
            assertThat(root.children().get(0).props()).containsEntry("current", "/about");
            assertThat(bodyMarker.get()).isEqualTo("about");
        }
    }

    @Test
    void fallsBackToFirstPageWhenStoredPathUnknown() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            session.state().put("__page__", "/unknown");
            AtomicReference<String> bodyMarker = new AtomicReference<>();
            Page home = new Page("Home", "/home", () -> bodyMarker.set("home"));
            RenderNode root = runner.render(session, () -> St.pages(List.of(home)));
            assertThat(root.children().get(0).props()).containsEntry("current", "/home");
            assertThat(bodyMarker.get()).isEqualTo("home");
        }
    }

    @Test
    void ignoresStoredNonStringValue() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            session.state().put("__page__", 42);
            AtomicReference<String> bodyMarker = new AtomicReference<>();
            Page home = new Page("Home", "/home", () -> bodyMarker.set("home"));
            runner.render(session, () -> St.pages(List.of(home)));
            assertThat(bodyMarker.get()).isEqualTo("home");
        }
    }
}
