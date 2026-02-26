package itmo.devops.aneki.model;

public record User(
        String id,
        String name,
        String email,
        String passwordHash
) {
}
