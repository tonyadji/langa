package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.User;

public interface UserService {
    User findOrCreateUserByEmail(String email);

    /**
     * Returns the local user bound to an identity provider account, creating it on first sign-in.
     * A local user without external identity and with the same email (legacy account or invited user)
     * is linked instead of creating a new one, so that its account key and data are preserved.
     */
    User provisionExternalUser(String externalId, String email);
}
