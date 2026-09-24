package com.langa.backend.domain.users.valueobjects;

/**
 * Identity of a user in an external identity provider (Entra ID, Cognito, Keycloak...).
 *
 * @param provider name of the identity provider, as configured
 * @param subject  stable identifier of the user in that provider
 * @param email    email address of the user in that provider
 */
public record ExternalIdentity(String provider, String subject, String email) {

    public ExternalIdentity {
        if (isBlank(provider) || isBlank(subject) || isBlank(email)) {
            throw new IllegalArgumentException("provider, subject and email are required");
        }
        email = email.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
