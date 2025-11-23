package com.langa.backend.infra.adapters.persistence.applications.mongo.daos;

import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationUsageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoApplicationUsageDao extends MongoRepository<ApplicationUsageDocument, String> {
    Optional<ApplicationUsageDocument> findByAppKey(String appKey);
}
