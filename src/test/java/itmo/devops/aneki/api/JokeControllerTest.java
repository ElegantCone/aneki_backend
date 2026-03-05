package itmo.devops.aneki.api;

import itmo.devops.aneki.api.dto.JokeResponse;
import itmo.devops.aneki.api.dto.JokeUpsertRequest;
import itmo.devops.aneki.api.dto.JokesResponse;
import itmo.devops.aneki.api.dto.OkResponse;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.JokeService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JokeControllerTest {

    @Test
    void listReturnsJokesResponse() {
        AuthService authService = mock(AuthService.class);
        JokeService jokeService = mock(JokeService.class);
        JokeController controller = new JokeController(jokeService, authService);
        User user = new User(UUID.randomUUID(), "User", "user@example.com", "hash");
        Joke joke = new Joke(UUID.randomUUID(), user.getId(), "new");
        when(authService.requireUserById(anyString())).thenReturn(user);
        when(jokeService.list()).thenReturn(List.of(joke));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getId().toString());

        JokesResponse response = controller.list(authentication);

        assertThat(response.jokes()).hasSize(1);
        assertThat(response.jokes().get(0).content()).isEqualTo("new");
        verify(authService, atLeastOnce()).requireUserById(anyString());
    }

    @Test
    void createReturnsJokeResponse() {
        AuthService authService = mock(AuthService.class);
        JokeService jokeService = mock(JokeService.class);
        JokeController controller = new JokeController(jokeService, authService);
        User user = new User(UUID.randomUUID(), "User", "user@example.com", "hash");
        Joke joke = new Joke(UUID.randomUUID(), user.getId(), "content");
        when(authService.requireUserById(user.getId().toString())).thenReturn(user);
        when(jokeService.create(user, "content")).thenReturn(joke);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getId().toString());

        JokeResponse response = controller.create(authentication, new JokeUpsertRequest("content"));

        assertThat(response.joke().content()).isEqualTo("content");
    }

    @Test
    void updateReturnsJokeResponse() {
        AuthService authService = mock(AuthService.class);
        JokeService jokeService = mock(JokeService.class);
        JokeController controller = new JokeController(jokeService, authService);
        User user = new User(UUID.randomUUID(), "User", "user@example.com", "hash");
        Joke joke = new Joke(UUID.randomUUID(), user.getId(), "content");
        when(authService.requireUserById(user.getId().toString())).thenReturn(user);
        when(jokeService.update(user, joke.getId().toString(), "new")).thenReturn(joke);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getId().toString());

        JokeResponse response = controller.update(joke.getId().toString(), authentication, new JokeUpsertRequest("new"));

        assertThat(response.joke().content()).isEqualTo("content");
    }

    @Test
    void deleteReturnsOkResponse() {
        AuthService authService = mock(AuthService.class);
        JokeService jokeService = mock(JokeService.class);
        JokeController controller = new JokeController(jokeService, authService);
        User user = new User(UUID.randomUUID(), "User", "user@example.com", "hash");
        when(authService.requireUserById(user.getId().toString())).thenReturn(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getId().toString());

        OkResponse response = controller.delete(UUID.randomUUID().toString(), authentication);

        assertThat(response.ok()).isTrue();
    }
}
