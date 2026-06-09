package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class Streamlit4jAutoConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Streamlit4jAutoConfiguration.class));

    private final ApplicationContextRunner nonWebContextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(Streamlit4jAutoConfiguration.class));

    @Test
    void loadsBeansInServletWebEnvironment() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(Streamlit4jAutoConfiguration.class);
            assertThat(context).hasSingleBean(Streamlit4jProperties.class);
            assertThat(context).hasSingleBean(EntrypointSource.class);
            assertThat(context).hasSingleBean(Streamlit4jApplication.class);
            assertThat(context).hasSingleBean(Streamlit4jWebSocketHandler.class);
        });
    }

    @Test
    void skipsAutoConfigurationOutsideWebEnvironment() {
        nonWebContextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(Streamlit4jAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(Streamlit4jWebSocketHandler.class);
        });
    }

    @Test
    void bindsBasePathFromProperties() {
        webContextRunner.withPropertyValues("streamlit4j.base-path=/apps/demo").run(context -> {
            Streamlit4jProperties props = context.getBean(Streamlit4jProperties.class);
            assertThat(props.getBasePath()).isEqualTo("/apps/demo");
        });
    }

    @Test
    void defaultBasePathIsStreamlit4j() {
        webContextRunner.run(context -> {
            Streamlit4jProperties props = context.getBean(Streamlit4jProperties.class);
            assertThat(props.getBasePath()).isEqualTo("/streamlit4j");
        });
    }

    @Test
    void honoursUserProvidedEntrypointSource() {
        EntrypointSource userSource = () -> () -> {};
        webContextRunner
                .withBean("customEntrypoint", EntrypointSource.class, () -> userSource)
                .run(context -> {
                    assertThat(context.getBean(EntrypointSource.class)).isSameAs(userSource);
                });
    }

    @Test
    void registersHandshakeInterceptorWhenSpringSecurityOnClasspath() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(Streamlit4jPrincipalHandshakeInterceptor.class);
        });
    }

    @Test
    void wiresHttpSessionIntegration() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(Streamlit4jHttpSessionRegistry.class);
            assertThat(context).hasSingleBean(Streamlit4jHttpSessionHandshakeInterceptor.class);
            assertThat(context).hasSingleBean(Streamlit4jHttpSessionBinder.class);
        });
    }

    @Test
    void wiresResourceRegistrationWhenWebMvcPresent() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(Streamlit4jAutoConfiguration.ResourceRegistration.class);
        });
    }
}
