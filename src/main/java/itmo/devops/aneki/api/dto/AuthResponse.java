package itmo.devops.aneki.api.dto;

public record AuthResponse(String token, UserDto user) {
}
