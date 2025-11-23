package com.langa.backend.domain.users.valueobjects;

import java.util.UUID;

public record UserId(
        String id,
        String email
) {
    public static UserId newId(String email) {
        return new UserId(UUID.fromString(email).toString(), email);
    }
}
