package com.langa.backend.domain.applications.services;

public record IngestionCredentials(
        String userAgent,
        String appKey,
        String accountKey,
        String timestamp,
        String signature,
        IngestionSecurity.CredentialType credentialType
) {
}
