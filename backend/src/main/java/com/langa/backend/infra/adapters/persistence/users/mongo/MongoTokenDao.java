package com.langa.backend.infra.adapters.persistence.users.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoTokenDao extends MongoRepository<TokenDocument, String> {
    Optional<TokenDocument> findByToken(String token);
    List<TokenDocument> findByUserEmail(String userEmail);

    void deleteByUserEmail(String userEmail);
}
