package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResult signup(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String safeName = requireNonBlank(name, "name");
        String safePassword = requireNonBlank(password, "password");

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        User user = new User(
                UUID.randomUUID(),
                safeName,
                normalizedEmail,
                passwordEncoder.encode(safePassword)
        );
        userRepository.save(user);

        String token = jwtService.issueToken(user);
        return new AuthResult(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String safePassword = requireNonBlank(password, "password");
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(safePassword, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.issueToken(user);
        return new AuthResult(token, user);
    }

    @Transactional(readOnly = true)
    public User requireUserFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header");
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String userId = jwtService.extractUserId(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token"));
    }

    private String normalizeEmail(String email) {
        String value = requireNonBlank(email, "email");
        return value.toLowerCase(Locale.ROOT);
    }

    private String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Field '" + field + "' is required");
        }
        return value.trim();
    }

    public record AuthResult(String token, User user) {
    }
}
