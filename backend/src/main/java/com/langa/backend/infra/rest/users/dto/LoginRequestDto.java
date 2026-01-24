package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.usecases.login.LoginCommand;

public record LoginRequestDto(String username, String password) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
