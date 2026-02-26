package itmo.devops.aneki.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    protected User() {
    }

    public User(UUID id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
