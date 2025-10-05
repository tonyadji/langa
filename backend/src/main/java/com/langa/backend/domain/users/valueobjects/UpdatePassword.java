package com.langa.backend.domain.users.valueobjects;

public record UpdatePassword(
        String password,
        String confirmationPassword
) {
}
