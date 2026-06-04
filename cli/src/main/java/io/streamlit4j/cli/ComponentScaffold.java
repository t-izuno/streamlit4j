package io.streamlit4j.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scaffolds a new iframe-isolated custom component project for streamlit4j.
 *
 * <p>The generated layout pairs with the Java-side {@code St.iframeComponent(...)}
 * declaration and the JavaScript-side {@code Streamlit4jComponent} SDK
 * (frontend/src/sdk). After scaffolding, the developer runs their own
 * bundler to serve {@code index.html}, then references it as the
 * {@code iframeSrc} in Java.
 */
public final class ComponentScaffold {

    private static final Pattern VALID_NAME = Pattern.compile("[a-z][a-z0-9-]*");

    private ComponentScaffold() {}

    public static void create(String name, Path targetDir) throws IOException {
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("component name must match [a-z][a-z0-9-]*: " + name);
        }
        if (Files.exists(targetDir)) {
            try (Stream<Path> entries = Files.list(targetDir)) {
                if (entries.findAny().isPresent()) {
                    throw new IllegalStateException("target directory is not empty: " + targetDir);
                }
            }
        } else {
            Files.createDirectories(targetDir);
        }
        write(targetDir.resolve("index.html"), indexHtml(name));
        write(targetDir.resolve("main.ts"), mainTs(name));
        write(targetDir.resolve("package.json"), packageJson(name));
        write(targetDir.resolve("README.md"), readme(name));
    }

    private static void write(Path file, String contents) throws IOException {
        Files.writeString(file, contents, StandardCharsets.UTF_8);
    }

    private static String indexHtml(String name) {
        return """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8" />
                    <title>%s</title>
                  </head>
                  <body>
                    <div id="root"></div>
                    <script type="module" src="./main.ts"></script>
                  </body>
                </html>
                """
                .formatted(name);
    }

    private static String mainTs(String name) {
        return """
                import { Streamlit4jComponent } from '@streamlit4j/component-sdk';

                const sdk = new Streamlit4jComponent<{ label?: string }, string>('%s');
                const root = document.getElementById('root');

                sdk.onState(({ args, value }) => {
                  if (!root) return;
                  root.innerHTML = '';
                  const button = document.createElement('button');
                  button.type = 'button';
                  button.textContent = `${args.label ?? '%s'}: ${value ?? ''}`;
                  button.addEventListener('click', () => sdk.setValue(new Date().toISOString()));
                  root.appendChild(button);
                });

                sdk.ready();
                """
                .formatted(name, name);
    }

    private static String packageJson(String name) {
        return """
                {
                  "name": "%s-streamlit4j-component",
                  "version": "0.1.0",
                  "private": true,
                  "type": "module",
                  "scripts": {
                    "dev": "vite",
                    "build": "vite build"
                  },
                  "dependencies": {
                    "@streamlit4j/component-sdk": "^0.1.0"
                  },
                  "devDependencies": {
                    "typescript": "^5.7.0",
                    "vite": "^6.0.0"
                  }
                }
                """
                .formatted(name);
    }

    private static String readme(String name) {
        return """
                # %s — streamlit4j custom component

                Iframe-isolated custom component scaffolded by `streamlit4j component create %s`.

                ## Develop

                ```sh
                npm install
                npm run dev
                ```

                The dev server will serve `index.html`; point your streamlit4j app at the
                resulting URL via:

                ```java
                CustomComponent<String> spec = new CustomComponent<>("%s", String.class);
                String value = St.iframeComponent(spec, "http://localhost:5173", Map.of("label", "Click"));
                ```

                ## Build

                `npm run build` emits a static bundle suitable for hosting from any
                static-file server.
                """
                .formatted(name, name, name);
    }
}
