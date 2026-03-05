package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceHelperTest {

    static class TestHelper extends ServiceHelper {
        void check(String value, String field, boolean spacesAllowed) {
            requireNonBlank(value, field, spacesAllowed);
        }
    }

    private final TestHelper helper = new TestHelper();

    @Test
    void requireNonBlankRejectsNull() {
        assertThatThrownBy(() -> helper.check(null, "field", true))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void requireNonBlankRejectsBlank() {
        assertThatThrownBy(() -> helper.check("   ", "field", true))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void requireNonBlankRejectsSpacesWhenNotAllowed() {
        assertThatThrownBy(() -> helper.check("has space", "field", false))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}
