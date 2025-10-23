package com.langa.backend.infra.adapters.persistence.applications.mongo.daos;

import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MongoApplicationDao extends MongoRepository<ApplicationDocument, String> {
    Optional<ApplicationDocument> findByKey(String key);

    Optional<ApplicationDocument> findByKeyAndAccountKey(String key, String accountKey);

    List<ApplicationDocument> findByAccountKey(String accountKey);
    List<ApplicationDocument> findByOwner(String owner);
    List<ApplicationDocument> findBySharedWith_key(String accountOrTeamKey);
    List<ApplicationDocument> findBySharedWith_KeyIn(Set<String> teamKeys);

    Optional<ApplicationDocument> findByOwnerAndName(String owner, String name);

    Optional<ApplicationDocument> findByIdAndOwner(String appId, String username);
}
