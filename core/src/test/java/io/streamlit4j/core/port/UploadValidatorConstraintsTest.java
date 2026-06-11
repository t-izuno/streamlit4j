package io.streamlit4j.core.port;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class UploadValidatorConstraintsTest {

    @Test
    void nullAllowedMimeTypesNormalizesToEmptySet() {
        UploadValidator.Constraints c = new UploadValidator.Constraints(1024, null);
        assertThat(c.allowedMimeTypes()).isEmpty();
        assertThat(c.maxSizeBytes()).isEqualTo(1024);
    }

    @Test
    void copiesGivenSet() {
        UploadValidator.Constraints c = new UploadValidator.Constraints(2048, Set.of("text/csv", "image/png"));
        assertThat(c.allowedMimeTypes()).containsExactlyInAnyOrder("text/csv", "image/png");
    }

    @Test
    void resultAcceptIsTrueWithEmptyReason() {
        UploadValidator.Result r = UploadValidator.Result.accept();
        assertThat(r.accepted()).isTrue();
        assertThat(r.reason()).isEmpty();
    }

    @Test
    void resultRejectIsFalseWithReason() {
        UploadValidator.Result r = UploadValidator.Result.reject("nope");
        assertThat(r.accepted()).isFalse();
        assertThat(r.reason()).isEqualTo("nope");
    }
}
