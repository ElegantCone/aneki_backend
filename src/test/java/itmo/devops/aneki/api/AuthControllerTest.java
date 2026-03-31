package itmo.devops.aneki.api;

import itmo.devops.aneki.ConfigurationHelper;
import itmo.devops.aneki.api.dto.*;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.AuthService.AuthResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest extends ConfigurationHelper {

    @Test
    void loginSetsAuthCookieAndReturnsUser() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, "access", 60000, true, "Strict");
        when(authService.login(user.getEmail(), rawPassword))
                .thenReturn(new AuthResult("token-1", user));

        ResponseEntity<AuthResponse> response = controller.login(new LoginRequest(user.getEmail(), rawPassword));

        ResponseCookie expectedCookie = ResponseCookie.from("access", "token-1")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(60000))
                .build();
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .isEqualTo(expectedCookie.toString());
        assertThat(response.getBody().user().email()).isEqualTo(user.getEmail());
    }

    @Test
    void signupSetsAuthCookieAndReturnsUser() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, "access", 60000, false, "Lax");
        when(authService.signup(user.getName(), user.getEmail(), rawPassword))
                .thenReturn(new AuthResult("token-2", user));

        ResponseEntity<AuthResponse> response = controller.signup(new SignupRequest(user.getName(), user.getEmail(), rawPassword));

        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("access=token-2");
        assertThat(response.getBody().user().name()).isEqualTo(user.getName());
    }

    @Test
    void logoutClearsAuthCookie() {
        AuthController controller = new AuthController(mock(AuthService.class), "access", 60000, false, "Lax");

        ResponseEntity<OkResponse> response = controller.logout();

        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("access=")
                .contains("Max-Age=0");
        assertThat(response.getBody().ok()).isTrue();
    }

    @Test
    void meReturnsUserDto() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, "access", 60000, false, "Lax");
        when(authService.requireUserById("user-id")).thenReturn(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user-id");

        MeResponse response = controller.me(authentication);

        assertThat(response.user().email()).isEqualTo(user.getEmail());
    }
}
