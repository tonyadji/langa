package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.AuthRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDtoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "secret-password";

    @Test
    void toAuthRequest_shouldMapCorrectly() {
        LoginRequestDto dto = new LoginRequestDto(EMAIL, PASSWORD);

        AuthRequest authRequest = dto.toAuthRequest();

        assertThat(authRequest.username()).isEqualTo("test@example.com");
        assertThat(authRequest.password()).isEqualTo("secret-password");
    }

    @Test
    void recordAccessors_shouldWorkCorrectly() {
        LoginRequestDto dto = new LoginRequestDto(EMAIL, PASSWORD);

        assertThat(dto.username()).isEqualTo("test@example.com");
        assertThat(dto.password()).isEqualTo("secret-password");
    }
}
