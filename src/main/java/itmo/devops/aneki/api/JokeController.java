package itmo.devops.aneki.api;

import itmo.devops.aneki.api.dto.JokeResponse;
import itmo.devops.aneki.api.dto.JokeUpsertRequest;
import itmo.devops.aneki.api.dto.JokesResponse;
import itmo.devops.aneki.api.dto.OkResponse;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.JokeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static itmo.devops.aneki.api.dto.JokeDto.toDto;

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
        return new JokesResponse(jokeService.list().stream().map(j -> toDto(j, authService)).toList());
    }

    @PostMapping
    public JokeResponse create(
            Authentication authentication,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserById(authentication.getName());
        Joke joke = jokeService.create(user, request.content());
        return new JokeResponse(toDto(joke, authService));
    }

    @PutMapping("/{id}")
    public JokeResponse update(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody JokeUpsertRequest request
    ) {
        User user = authService.requireUserById(authentication.getName());
        Joke joke = jokeService.update(user, id, request.content());
        return new JokeResponse(toDto(joke, authService));
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
}
