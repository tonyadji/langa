package com.langa.backend.infra.adapters.in.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.LoginCommand;

public record LoginRequestDto(String username, String password) {
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
