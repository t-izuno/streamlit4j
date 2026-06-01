package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Streamlit4jAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(Streamlit4jAutoConfiguration.class));

    @Test
    void loadsAutoConfigurationWhenCoreOnClasspath() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Streamlit4jAutoConfiguration.class);
            assertThat(context).hasSingleBean(Streamlit4jProperties.class);
        });
    }

    @Test
    void bindsBasePathFromProperties() {
        contextRunner.withPropertyValues("streamlit4j.base-path=/apps/demo").run(context -> {
            Streamlit4jProperties props = context.getBean(Streamlit4jProperties.class);
            assertThat(props.getBasePath()).isEqualTo("/apps/demo");
        });
    }

    @Test
    void defaultBasePathIsStreamlit() {
        contextRunner.run(context -> {
            Streamlit4jProperties props = context.getBean(Streamlit4jProperties.class);
            assertThat(props.getBasePath()).isEqualTo("/streamlit");
        });
    }
}
