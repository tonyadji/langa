package com.langa.backend.domain.applications.services;

import com.langa.backend.domain.applications.Application;

public interface IngestionSecurity {
    boolean isAuthorized(IngestionCredentials credentials, Application app);

    enum CredentialType {
        HTTP,
        KAFKA,
        MQ
    }
}
