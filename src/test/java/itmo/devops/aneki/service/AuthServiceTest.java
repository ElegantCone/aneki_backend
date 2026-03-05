package itmo.devops.aneki.service;

import itmo.devops.aneki.ConfigurationHelper;
import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest extends ConfigurationHelper {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void signupCreatesUserAndIssuesToken() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(jwtService.issueToken(any(User.class))).thenReturn("token-123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthResult result = authService.signup(user.getName(), user.getEmail(), rawPassword);

        assertThat(result.token()).isEqualTo("token-123");
        assertThat(result.user().getEmail()).isEqualTo(user.getEmail());
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(user.getName());
        assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, saved.getPasswordHash())).isTrue();
    }

    @Test
    void signupRejectsDuplicateEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup("diff_username", user.getEmail(), "dif_pass"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void signupRejectsBlankFields() {
        assertThatThrownBy(() -> authService.signup("", user.getEmail(), rawPassword))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.issueToken(user)).thenReturn("token-xyz");

        AuthService.AuthResult result = authService.login(user.getEmail(), rawPassword);

        assertThat(result.token()).isEqualTo("token-xyz");
        assertThat(result.user()).isEqualTo(user);
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("missing@example.com", "pass"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void loginRejectsWrongPassword() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(user.getEmail(), "wrong"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void requireUserByIdRejectsInvalidUuid() {
        assertThatThrownBy(() -> authService.requireUserById("not-a-uuid"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void requireUserByIdRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requireUserById(userId.toString()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }
}
