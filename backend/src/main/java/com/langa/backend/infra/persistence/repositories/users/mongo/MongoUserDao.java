package com.langa.backend.infra.persistence.repositories.users.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoUserDao extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);

    Optional<UserDocument> findByFirstConnectionToken(String token);
}
