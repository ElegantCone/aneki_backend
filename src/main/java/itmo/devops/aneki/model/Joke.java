package itmo.devops.aneki.model;

public record Joke(
        String id,
        String userId,
        String userName,
        String content,
        long createdAt,
        long updatedAt
) {
    public Joke withContent(String newContent, long updatedAtMillis) {
        return new Joke(id, userId, userName, newContent, createdAt, updatedAtMillis);
    }
}
