package itmo.devops.aneki.api.dto;

import itmo.devops.aneki.model.User;

import java.util.UUID;

public record UserDto(UUID id, String name, String email) {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}

