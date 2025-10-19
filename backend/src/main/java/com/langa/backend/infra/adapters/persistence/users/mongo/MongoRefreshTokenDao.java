package com.langa.backend.infra.adapters.persistence.users.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoRefreshTokenDao extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByToken(String token);

    void deleteByUserEmail(String userEmail);
}
