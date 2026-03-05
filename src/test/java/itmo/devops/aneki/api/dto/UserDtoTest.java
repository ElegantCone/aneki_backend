package itmo.devops.aneki.api.dto;

import itmo.devops.aneki.ConfigurationHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest extends ConfigurationHelper {

    @Test
    void toUserDtoMapsFields() {
        UserDto dto = UserDto.toUserDto(user);
        assertThat(dto.id()).isEqualTo(user.getId());
        assertThat(dto.name()).isEqualTo(user.getName());
        assertThat(dto.email()).isEqualTo(user.getEmail());
    }
}
