package com.langa.backend.domain.users.valueobjects;

import java.util.UUID;

public record UserId(
        String id,
        String email,
        String accountKey
) {

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString(), null, null);
    }

    public static UserId of(String id, String email, String accountKey) {
        return new UserId(id, email, accountKey);
    }

    public UserId withEmail(String email) {
        return new UserId(id, email, accountKey);
    }

    public UserId withAccountKey(String accountKey) {
        return new UserId(id, email, accountKey);
    }
}
