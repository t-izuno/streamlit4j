package io.streamlit4j.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentScaffoldTest {

    @Test
    void scaffoldsFourTemplateFiles(@TempDir Path tmp) throws IOException {
        Path target = tmp.resolve("my-widget");

        ComponentScaffold.create("my-widget", target);

        assertThat(target.resolve("index.html")).exists();
        assertThat(target.resolve("main.ts")).exists();
        assertThat(target.resolve("package.json")).exists();
        assertThat(target.resolve("README.md")).exists();
    }

    @Test
    void substitutesNameIntoTemplates(@TempDir Path tmp) throws IOException {
        Path target = tmp.resolve("color-picker");

        ComponentScaffold.create("color-picker", target);

        String mainTs = Files.readString(target.resolve("main.ts"));
        assertThat(mainTs).contains("new Streamlit4jComponent");
        assertThat(mainTs).contains("'color-picker'");

        String pkg = Files.readString(target.resolve("package.json"));
        assertThat(pkg).contains("\"color-picker-streamlit4j-component\"");
        assertThat(pkg).contains("@streamlit4j/component-sdk");

        String readme = Files.readString(target.resolve("README.md"));
        assertThat(readme).contains("streamlit4j component create color-picker");
        assertThat(readme).contains("St.iframeComponent(spec");
    }

    @Test
    void rejectsInvalidComponentName(@TempDir Path tmp) {
        Path target = tmp.resolve("out");
        assertThatThrownBy(() -> ComponentScaffold.create("Invalid_Name", target))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("component name");
        assertThatThrownBy(() -> ComponentScaffold.create("../escape", target))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ComponentScaffold.create("", target)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonEmptyTargetDirectory(@TempDir Path tmp) throws IOException {
        Path target = tmp.resolve("existing");
        Files.createDirectories(target);
        Files.writeString(target.resolve("existing.txt"), "hello");

        assertThatThrownBy(() -> ComponentScaffold.create("widget", target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not empty");
    }

    @Test
    void allowsEmptyExistingDirectory(@TempDir Path tmp) throws IOException {
        Path target = tmp.resolve("empty-dir");
        Files.createDirectories(target);

        ComponentScaffold.create("widget", target);

        assertThat(target.resolve("index.html")).exists();
    }
}
