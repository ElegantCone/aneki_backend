package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService extends ServiceHelper {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthResult signup(String name, String email, String password) {
        requireNonBlank(email, "email", false);
        requireNonBlank(name, "name", false);
        requireNonBlank(password, "password", false);

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        User user = new User(
                UUID.randomUUID(),
                name,
                email,
                passwordEncoder.encode(password)
        );
        userRepository.save(user);

        String token = jwtService.issueToken(user);
        return new AuthResult(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        requireNonBlank(email, "email", false);
        requireNonBlank(password, "password", false);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.issueToken(user);
        return new AuthResult(token, user);
    }

    @Transactional(readOnly = true)
    public User requireUserById(String userId) {
        try {
            return userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token"));
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public record AuthResult(String token, User user) {
    }
}
