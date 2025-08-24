package com.langa.backend.infra.persistence.repositories.applications.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoApplicationDao extends MongoRepository<ApplicationDocument, String> {
    Optional<ApplicationDocument> findByKey(String key);

    Optional<ApplicationDocument> findByKeyAndAccountKey(String key, String accountKey);

    List<ApplicationDocument> findByAccountKey(String accountKey);
    List<ApplicationDocument> findByOwner(String owner);

    Optional<ApplicationDocument> findByOwnerAndName(String owner, String name);

    Optional<ApplicationDocument> findByIdAndOwner(String appId, String username);
}
