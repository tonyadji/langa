package com.langa.backend.infra.adapters.persistence.users.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoUserDao extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);

    Optional<UserDocument> findFirstByEmailIgnoreCase(String email);

    Optional<UserDocument> findByIdentityProviderAndExternalId(String identityProvider, String externalId);

    Optional<UserDocument> findByEmailOrAccountKey(String email, String accountKey);
}
