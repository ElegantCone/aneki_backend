package itmo.devops.aneki.api;

import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.AuthService.AuthResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final String authCookieName;
    private final long jwtTtlMs;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.cookie-name:access_token}") String authCookieName,
            @Value("${app.jwt.ttl-ms}") long jwtTtlMs,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.authService = authService;
        this.authCookieName = authCookieName;
        this.jwtTtlMs = jwtTtlMs;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResult result = authService.login(request.email(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie(result.token()).toString())
                .body(new AuthResponse(result.token(), toUserDto(result.user())));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        AuthResult result = authService.signup(request.name(), request.email(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie(result.token()).toString())
                .body(new AuthResponse(result.token(), toUserDto(result.user())));
    }

    @PostMapping("/logout")
    public ResponseEntity<OkResponse> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAuthCookie().toString())
                .body(new OkResponse(true));
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        User user = authService.requireUserById(authentication.getName());
        return new MeResponse(toUserDto(user));
    }

    private ResponseCookie authCookie(String token) {
        return ResponseCookie.from(authCookieName, token)
                .httpOnly(true)
                .secure(authCookieSecure)
                .path("/")
                .sameSite(authCookieSameSite)
                .maxAge(Duration.ofMillis(jwtTtlMs))
                .build();
    }

    private ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(authCookieName, "")
                .httpOnly(true)
                .secure(authCookieSecure)
                .path("/")
                .sameSite(authCookieSameSite)
                .maxAge(Duration.ZERO)
                .build();
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
