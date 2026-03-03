package itmo.devops.aneki.api.dto;

import itmo.devops.aneki.model.Joke;
import itmo.devops.aneki.service.AuthService;

import java.util.UUID;

public record JokeDto(
        UUID id,
        UUID userId,
        String username,
        String content,
        long createdAt,
        long updatedAt
) {

    public static JokeDto toDto(Joke joke, AuthService authService) {
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
