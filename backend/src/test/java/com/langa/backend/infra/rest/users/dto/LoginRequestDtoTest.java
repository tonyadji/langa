package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.LoginCommand;
import com.langa.backend.infra.adapters.in.rest.users.dto.LoginRequestDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDtoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "secret-password";

    @Test
    void toCommand_shouldMapCorrectly() {
        LoginRequestDto dto = new LoginRequestDto(EMAIL, PASSWORD);

        LoginCommand loginCommand = dto.toCommand();

        assertThat(loginCommand.username()).isEqualTo("test@example.com");
        assertThat(loginCommand.password()).isEqualTo("secret-password");
    }

    @Test
    void recordAccessors_shouldWorkCorrectly() {
        LoginRequestDto dto = new LoginRequestDto(EMAIL, PASSWORD);

        assertThat(dto.username()).isEqualTo("test@example.com");
        assertThat(dto.password()).isEqualTo("secret-password");
    }
}
