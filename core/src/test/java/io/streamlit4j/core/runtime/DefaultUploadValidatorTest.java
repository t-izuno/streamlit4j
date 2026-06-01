package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.port.UploadValidator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultUploadValidatorTest {

    @Test
    void acceptsFileWithinSizeAndAllowedMime() {
        var validator = new DefaultUploadValidator();
        var constraints = new UploadValidator.Constraints(1024, Set.of("text/csv"));

        var result = validator.validate("a.csv", "text/csv", 512, constraints);

        assertThat(result.accepted()).isTrue();
    }

    @Test
    void rejectsOversizedFile() {
        var validator = new DefaultUploadValidator();
        var constraints = new UploadValidator.Constraints(100, Set.of());

        var result = validator.validate("big.bin", "application/octet-stream", 200, constraints);

        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).contains("max size");
    }

    @Test
    void rejectsDisallowedMime() {
        var validator = new DefaultUploadValidator();
        var constraints = new UploadValidator.Constraints(Long.MAX_VALUE, Set.of("text/csv"));

        var result = validator.validate("a.png", "image/png", 1, constraints);

        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).contains("mime type");
    }
}
