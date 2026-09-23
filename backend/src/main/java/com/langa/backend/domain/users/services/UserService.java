package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;

import java.util.Optional;

public interface UserService {
    User findOrCreateUserByEmail(String email);

    /**
     * Returns the local user already bound to an identity provider account, if any.
     */
    Optional<User> findByExternalIdentity(String provider, String subject);

    /**
     * Returns the local user bound to an identity provider account, creating it on first sign-in.
     * A local user without external identity and with the same email (legacy account or invited user)
     * is linked instead of creating a new one, so that its account key and data are preserved.
     */
    User provisionExternalUser(ExternalIdentity identity);
}
