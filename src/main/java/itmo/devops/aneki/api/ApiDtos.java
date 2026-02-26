package itmo.devops.aneki.api;

record LoginRequest(String email, String password) {
}

record SignupRequest(String name, String email, String password) {
}

record UserDto(String id, String name, String email) {
}

record AuthResponse(String token, UserDto user) {
}

record MeResponse(UserDto user) {
}

record JokeDto(
        String id,
        String userId,
        String userName,
        String content,
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
