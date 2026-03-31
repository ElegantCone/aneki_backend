package itmo.devops.aneki.api.dto;

import java.util.List;

public record JokesResponse(List<JokeDto> jokes) {
}
