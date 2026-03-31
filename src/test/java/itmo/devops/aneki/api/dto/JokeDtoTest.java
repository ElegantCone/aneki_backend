package itmo.devops.aneki.api.dto;

import itmo.devops.aneki.ConfigurationHelper;
import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.service.AuthService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JokeDtoTest extends ConfigurationHelper {

    @Test
    void toDtoResolvesAuthorName() {
        AuthService authService = mock(AuthService.class);
        Joke joke = new Joke(UUID.randomUUID(), user.getId(), "content");
        when(authService.requireUserById(user.getId().toString())).thenReturn(user);

        JokeDto dto = JokeDto.toDto(joke, authService);

        assertThat(dto.username()).isEqualTo(user.getName());
        assertThat(dto.userId()).isEqualTo(user.getId());
        verify(authService).requireUserById(user.getId().toString());
    }
}
