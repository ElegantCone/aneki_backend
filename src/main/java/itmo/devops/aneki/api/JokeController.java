package itmo.devops.aneki.api;

import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.JokeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public JokesResponse list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        authService.requireUserFromAuthorizationHeader(authorizationHeader);
        return new JokesResponse(jokeService.list().stream().map(this::toDto).toList());
    }

    @PostMapping
    public JokeResponse create(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserFromAuthorizationHeader(authorizationHeader);
        Joke joke = jokeService.create(user, request.content());
        return new JokeResponse(toDto(joke));
    }

    @PutMapping("/{id}")
    public JokeResponse update(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserFromAuthorizationHeader(authorizationHeader);
        Joke joke = jokeService.update(user, id, request.content());
        return new JokeResponse(toDto(joke));
    }

    @DeleteMapping("/{id}")
    public OkResponse delete(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        User user = authService.requireUserFromAuthorizationHeader(authorizationHeader);
        jokeService.delete(user, id);
        return new OkResponse(true);
    }

    private JokeDto toDto(Joke joke) {
        return new JokeDto(
                joke.id(),
                joke.userId(),
                joke.userName(),
                joke.content(),
                joke.createdAt(),
                joke.updatedAt()
        );
    }
}
