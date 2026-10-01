package com.langa.backend.infra.security.localauth;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Identity of the users signed in with the local authentication mode.
 * The subject is derived from the email, so the same email always gets the same local user
 * (including the demo user created by the demo data seeder).
 */
public final class LocalIdentity {

    /** Identity provider name to configure ({@code application.security.auth.provider}) in local mode. */
    public static final String PROVIDER = "local";

    private LocalIdentity() {
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String subjectFor(String email) {
        return UUID.nameUUIDFromBytes((PROVIDER + ":" + normalizeEmail(email)).getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
