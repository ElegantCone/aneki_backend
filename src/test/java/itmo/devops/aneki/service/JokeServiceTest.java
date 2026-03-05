package itmo.devops.aneki.service;

import itmo.devops.aneki.ConfigurationHelper;
import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.repository.JokeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JokeServiceTest extends ConfigurationHelper {

    @Mock
    private JokeRepository jokeRepository;

    @InjectMocks
    private JokeService jokeService;

    @Captor
    private ArgumentCaptor<Joke> jokeCaptor;

    @Test
    void listReturnsRepositoryResults() {
        List<Joke> jokes = List.of(new Joke(UUID.randomUUID(), UUID.randomUUID(), "hi"));
        when(jokeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(jokes);

        List<Joke> result = jokeService.list();

        assertThat(result).isSameAs(jokes);
    }

    @Test
    void createPersistsJoke() {
        when(jokeRepository.save(any(Joke.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Joke created = jokeService.create(user, "content");

        verify(jokeRepository).save(jokeCaptor.capture());
        Joke saved = jokeCaptor.getValue();

        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getContent()).isEqualTo("content");
        assertThat(saved.getCreatedAt()).isPositive();
        assertThat(saved.getUpdatedAt()).isPositive();
        assertThat(created).isEqualTo(saved);
    }

    @Test
    void updateChangesContentAndTimestamp() {
        Joke existing = new Joke(UUID.randomUUID(), user.getId(), "old");
        long previousUpdatedAt = existing.getUpdatedAt();
        when(jokeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(jokeRepository.save(any(Joke.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Joke updated = jokeService.update(user, existing.getId().toString(), "new");

        assertThat(updated.getContent()).isEqualTo("new");
        assertThat(updated.getUpdatedAt()).isGreaterThanOrEqualTo(previousUpdatedAt);
    }

    @Test
    void updateRejectsNonOwner() {
        Joke existing = new Joke(UUID.randomUUID(), UUID.randomUUID(), "old");
        when(jokeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> jokeService.update(user, existing.getId().toString(), "new"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    @Test
    void updateRejectsMissingJoke() {
        when(jokeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jokeService.update(user, UUID.randomUUID().toString(), "new"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void deleteRemovesJoke() {
        Joke existing = new Joke(UUID.randomUUID(), user.getId(), "old");
        when(jokeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        jokeService.delete(user, existing.getId().toString());

        verify(jokeRepository).delete(existing);
    }

    @Test
    void deleteRejectsInvalidId() {
        assertThatThrownBy(() -> jokeService.delete(user, "bad"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void deleteRejectsNonOwner() {
        Joke existing = new Joke(UUID.randomUUID(), UUID.randomUUID(), "old");
        when(jokeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> jokeService.delete(user, existing.getId().toString()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }
}
