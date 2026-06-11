package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class Streamlit4jResourceRegistrationTest {

    @Test
    void registersAtDefaultBasePath() {
        assertThat(handlersFor("/streamlit4j")).containsExactly("/streamlit4j/**");
    }

    @Test
    void honoursCustomBasePath() {
        assertThat(handlersFor("/apps/demo")).containsExactly("/apps/demo/**");
    }

    @Test
    void normalisesTrailingSlash() {
        assertThat(handlersFor("/apps/demo/")).containsExactly("/apps/demo/**");
    }

    @Test
    void skipsRegistrationAtRoot() {
        assertThat(handlersFor("/")).isEmpty();
    }

    @Test
    void skipsRegistrationWhenBlank() {
        assertThat(handlersFor("")).isEmpty();
    }

    @Test
    void mappingPointsToBundledAssetsLocation() {
        assertThat(Streamlit4jAutoConfiguration.ResourceRegistration.ASSET_LOCATION)
                .isEqualTo("classpath:/META-INF/resources/streamlit4j/");
    }

    private static List<String> handlersFor(String basePath) {
        Streamlit4jProperties properties = new Streamlit4jProperties();
        properties.setBasePath(basePath);
        Streamlit4jAutoConfiguration.ResourceRegistration registration = new Streamlit4jAutoConfiguration.ResourceRegistration(
                properties);

        CapturingResourceRegistry registry = new CapturingResourceRegistry();
        registration.addResourceHandlers(registry);
        return registry.handlers;
    }

    private static final class CapturingResourceRegistry extends ResourceHandlerRegistry {

        private final List<String> handlers = new ArrayList<>();

        CapturingResourceRegistry() {
            super(new GenericWebApplicationContext(), null);
        }

        @Override
        public ResourceHandlerRegistration addResourceHandler(String... pathPatterns) {
            for (String pattern : pathPatterns) {
                handlers.add(pattern);
            }
            return new ResourceHandlerRegistration(pathPatterns);
        }
    }
}
