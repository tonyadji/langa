package com.langa.backend.infra.persistence.repositories.applications.mongo.daos;

import com.langa.backend.infra.persistence.repositories.applications.mongo.documents.ApplicationUsageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoApplicationUsageDao extends MongoRepository<ApplicationUsageDocument, String> {
    Optional<ApplicationUsageDocument> findByAppKey(String appKey);
}
