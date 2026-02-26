package itmo.devops.aneki.api;

import java.util.UUID;

record LoginRequest(String email, String password) {
}

record SignupRequest(String name, String email, String password) {
}

record UserDto(UUID id, String name, String email) {
}

record AuthResponse(String token, UserDto user) {
}

record MeResponse(UserDto user) {
}

record JokeDto(
        UUID id,
        UUID userId,
        String joke,
        long createdAt,
        long updatedAt
) {
}

record JokesResponse(java.util.List<JokeDto> jokes) {
}

record JokeResponse(JokeDto joke) {
}

record JokeUpsertRequest(String content) {
}

record OkResponse(boolean ok) {
}
