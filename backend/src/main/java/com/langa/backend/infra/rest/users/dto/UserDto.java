package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.UserInfo;

public record UserDto(String email, String accountKey) {
    public static UserDto of(UserInfo me) {
        return new UserDto(me.email(), me.accountKey());
    }
}
