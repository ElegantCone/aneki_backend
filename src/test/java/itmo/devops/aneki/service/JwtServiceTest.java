package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void issueAndExtractValidTokenReturnsEqualTokens() {
        JwtService jwtService = new JwtService("01234567890123456789012345678901", 60000);
        jwtService.init();
        User user = new User(UUID.randomUUID(), "User", "user@example.com", "hash");

        String token = jwtService.issueToken(user);
        String extracted = jwtService.extractUserId(token);

        assertThat(token).isNotBlank();
        assertThat(extracted).isEqualTo(user.getId().toString());
    }

    @Test
    void extractRejectsInvalidToken() {
        JwtService jwtService = new JwtService("01234567890123456789012345678901", 60000);
        jwtService.init();

        assertThatThrownBy(() -> jwtService.extractUserId("bad.token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void initRejectsShortSecret() {
        JwtService jwtService = new JwtService("short", 1000);

        assertThatThrownBy(jwtService::init)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void initRejectsNullSecrets() {
        JwtService jwtService = new JwtService(null, 1000);
        assertThatThrownBy(jwtService::init)
                .isInstanceOf(IllegalStateException.class);
    }
}
