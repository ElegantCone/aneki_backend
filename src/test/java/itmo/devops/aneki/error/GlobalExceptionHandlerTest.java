package itmo.devops.aneki.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsStatusAndMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiException exception = new ApiException(HttpStatus.BAD_REQUEST, "Bad input");

        ResponseEntity<ErrorResponse> response = handler.handleApiException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Bad input");
    }

    @Test
    void handleUnexpectedReturnsInternalServerError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }
}
