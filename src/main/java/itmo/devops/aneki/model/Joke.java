package itmo.devops.aneki.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jokes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Joke {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Setter
    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

    public Joke(UUID id, UUID userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = Instant.now().toEpochMilli();
        this.updatedAt = Instant.now().toEpochMilli();
    }

}
