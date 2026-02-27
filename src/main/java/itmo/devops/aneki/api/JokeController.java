package itmo.devops.aneki.api;

import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.JokeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jokes")
public class JokeController {

    private final JokeService jokeService;
    private final AuthService authService;

    public JokeController(JokeService jokeService, AuthService authService) {
        this.jokeService = jokeService;
        this.authService = authService;
    }

    @GetMapping
    public JokesResponse list(Authentication authentication) {
        authService.requireUserById(authentication.getName());
        return new JokesResponse(jokeService.list().stream().map(this::toDto).toList());
    }

    @PostMapping
    public JokeResponse create(
            Authentication authentication,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserById(authentication.getName());
        Joke joke = jokeService.create(user, request.content());
        return new JokeResponse(toDto(joke));
    }

    @PutMapping("/{id}")
    public JokeResponse update(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserById(authentication.getName());
        Joke joke = jokeService.update(user, id, request.content());
        return new JokeResponse(toDto(joke));
    }

    @DeleteMapping("/{id}")
    public OkResponse delete(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = authService.requireUserById(authentication.getName());
        jokeService.delete(user, id);
        return new OkResponse(true);
    }

    private JokeDto toDto(Joke joke) {
        return new JokeDto(
                joke.getId(),
                joke.getUserId(),
                authService.requireUserById(joke.getUserId().toString()).getName(),
                joke.getContent(),
                joke.getCreatedAt(),
                joke.getUpdatedAt()
        );
    }
}
