package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JokeService {

    private final Map<String, Joke> jokesById = new ConcurrentHashMap<>();
    private final AtomicLong jokeSeq = new AtomicLong(1);

    public List<Joke> list() {
        return jokesById.values().stream()
                .sorted(Comparator.comparingLong(Joke::createdAt).reversed())
                .toList();
    }

    public Joke create(User author, String content) {
        String safeContent = requireContent(content);
        long now = Instant.now().toEpochMilli();
        String id = "j_" + jokeSeq.getAndIncrement();
        Joke joke = new Joke(id, author.id(), author.name(), safeContent, now, now);
        jokesById.put(id, joke);
        return joke;
    }

    public Joke update(User actor, String jokeId, String content) {
        String safeContent = requireContent(content);
        Joke existing = getRequired(jokeId);
        requireOwner(actor, existing);
        Joke updated = existing.withContent(safeContent, Instant.now().toEpochMilli());
        jokesById.put(jokeId, updated);
        return updated;
    }

    public void delete(User actor, String jokeId) {
        Joke existing = getRequired(jokeId);
        requireOwner(actor, existing);
        jokesById.remove(jokeId);
    }

    private Joke getRequired(String jokeId) {
        Joke joke = jokesById.get(jokeId);
        if (joke == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Joke not found");
        }
        return joke;
    }

    private void requireOwner(User actor, Joke joke) {
        if (!joke.userId().equals(actor.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can modify only your own jokes");
        }
    }

    private String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Field 'content' is required");
        }
        return content.trim();
    }
}
