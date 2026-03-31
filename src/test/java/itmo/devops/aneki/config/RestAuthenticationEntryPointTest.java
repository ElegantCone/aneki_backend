package itmo.devops.aneki.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RestAuthenticationEntryPointTest {

    @Test
    void commenceWritesUnauthorizedResponse() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }
}
