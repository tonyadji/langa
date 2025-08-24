package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.UserInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    private static final String USERNAME = "test@example.com";
    private static final String ACCOUNT_KEY = "ACCOUNT-123";
    @Test
    void of_shouldMapFromUserInfo() {
        UserInfo userInfo = new UserInfo(USERNAME, ACCOUNT_KEY);

        UserDto dto = UserDto.of(userInfo);

        assertThat(dto.email()).isEqualTo("test@example.com");
        assertThat(dto.accountKey()).isEqualTo("ACCOUNT-123");
    }

    @Test
    void recordAccessors_shouldWorkCorrectly() {
        UserDto dto = new UserDto(USERNAME, ACCOUNT_KEY);

        assertThat(dto.email()).isEqualTo("test@example.com");
        assertThat(dto.accountKey()).isEqualTo("ACCOUNT-123");
    }
}
