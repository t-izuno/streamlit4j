package io.streamlit4j.springboot;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

/**
 * Prints a streamlit4j-specific startup banner with the resolved local URL
 * after the Spring Boot application has finished starting up. Mirrors the
 * banner that {@code Streamlit4jServer} prints in standalone (embedded) use,
 * so both adoption paths surface the URL the user should open.
 */
public class Streamlit4jStartupBanner {

    private final Streamlit4jProperties properties;

    /**
     * Creates the banner listener.
     *
     * @param properties streamlit4j configuration (used to resolve base-path)
     */
    public Streamlit4jStartupBanner(Streamlit4jProperties properties) {
        this.properties = properties;
    }

    /**
     * Reacts to the application-ready event and prints the local URL.
     *
     * @param event ready event carrying the running application context
     */
    @EventListener
    @SuppressWarnings("PMD.SystemPrintln")
    public void onReady(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        if (!(context instanceof WebServerApplicationContext webContext) || webContext.getWebServer() == null) {
            return;
        }
        int port = webContext.getWebServer().getPort();
        String basePath = Streamlit4jAutoConfiguration.normalizeBasePath(properties.getBasePath());
        String mount = basePath.isEmpty() ? "/" : basePath;
        String url = "http://localhost:" + port + mount;
        System.out.println();
        System.out.println("  streamlit4j is ready.");
        System.out.println();
        System.out.println("  Local URL: " + url);
        System.out.println("  WebSocket: ws://localhost:" + port + basePath + "/ws");
        System.out.println();
    }
}
