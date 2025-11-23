package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.infra.adapters.in.rest.users.dto.LoginResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseDtoTest {

    @Test
    void of_shouldMapFromAuthToken() {
        AuthTokens authToken = new AuthTokens("my-secure-token", "my-refresh-token");

        LoginResponseDto dto = LoginResponseDto.of(authToken);

        assertThat(dto.accessToken()).isEqualTo("my-secure-token");
    }

    @Test
    void recordAccessors_shouldWorkCorrectly() {
        LoginResponseDto dto = new LoginResponseDto("secured-token-123", "my-refresh-token");

        assertThat(dto.accessToken()).isEqualTo("secured-token-123");
    }
}
