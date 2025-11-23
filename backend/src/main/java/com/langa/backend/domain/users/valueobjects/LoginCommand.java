package com.langa.backend.domain.users.valueobjects;

public record LoginCommand(String username, String password) {

    public void validate() {
    }
}
