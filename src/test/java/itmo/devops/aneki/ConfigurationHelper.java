package itmo.devops.aneki;

import itmo.devops.aneki.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class ConfigurationHelper {

    protected BCryptPasswordEncoder encoder;
    protected User user;
    protected String rawPassword;


    @BeforeEach
    public void initUser() {
        encoder = new BCryptPasswordEncoder();
        rawPassword = "secret";
        user = new User(UUID.randomUUID(), "User", "user@example.com", encoder.encode(rawPassword));
    }
}
