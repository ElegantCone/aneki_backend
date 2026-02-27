package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.repository.JokeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JokeService {

    private final JokeRepository jokeRepository;

    public JokeService(JokeRepository jokeRepository) {
        this.jokeRepository = jokeRepository;
    }

    @Transactional(readOnly = true)
    public List<Joke> list() {
        return jokeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Joke create(User author, String content) {
        String safeContent = requireContent(content);
        long now = Instant.now().toEpochMilli();
        Joke joke = new Joke(
                UUID.randomUUID(),
                author.getId(),
                safeContent,
                now,
                now
        );
        return jokeRepository.save(joke);
    }

    @Transactional
    public Joke update(User actor, String jokeId, String content) {
        String safeContent = requireContent(content);
        Joke existing = getRequired(jokeId);
        requireOwner(actor, existing);
        existing.setContent(safeContent);
        existing.setUpdatedAt(Instant.now().toEpochMilli());
        return jokeRepository.save(existing);
    }

    @Transactional
    public void delete(User actor, String jokeId) {
        Joke existing = getRequired(jokeId);
        requireOwner(actor, existing);
        jokeRepository.delete(existing);
    }

    private Joke getRequired(String jokeId) {
        try {
            return jokeRepository.findById(UUID.fromString(jokeId))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Joke not found"));
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Joke not found");
        }
    }

    private void requireOwner(User actor, Joke joke) {
        if (!joke.getUserId().equals(actor.getId())) {
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
