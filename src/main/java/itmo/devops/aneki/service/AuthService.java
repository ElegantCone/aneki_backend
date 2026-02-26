package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthService {

    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByEmail = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByToken = new ConcurrentHashMap<>();
    private final AtomicLong userSeq = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService() {
        //todo for testing only, remove later
        signup("Анна", "user@example.com", "secret");
    }

    public AuthResult signup(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String safeName = requireNonBlank(name, "name");
        String safePassword = requireNonBlank(password, "password");

        if (userIdByEmail.containsKey(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        String userId = "u_" + userSeq.getAndIncrement();
        User user = new User(userId, safeName, normalizedEmail, passwordEncoder.encode(safePassword));
        usersById.put(userId, user);
        userIdByEmail.put(normalizedEmail, userId);

        String token = issueToken(userId);
        return new AuthResult(token, user);
    }

    public AuthResult login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String safePassword = requireNonBlank(password, "password");
        String userId = userIdByEmail.get(normalizedEmail);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = usersById.get(userId);
        if (user == null || !passwordEncoder.matches(safePassword, user.passwordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = issueToken(user.id());
        return new AuthResult(token, user);
    }

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

        String userId = userIdByToken.get(token);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        User user = usersById.get(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return user;
    }

    private String issueToken(String userId) {
        String token = UUID.randomUUID().toString();
        userIdByToken.put(token, userId);
        return token;
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
