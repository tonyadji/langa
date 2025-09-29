package com.langa.backend.infra.persistence.repositories.teams.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoTeamDao extends MongoRepository<TeamDocument, String> {
    Optional<TeamDocument> findByCreatedByAndName(String owner, String name);

    Optional<TeamDocument> findByKey(String key);

    List<TeamDocument> findByCreatedBy(String owner);
}
