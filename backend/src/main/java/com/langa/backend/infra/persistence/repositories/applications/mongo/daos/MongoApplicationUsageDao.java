package com.langa.backend.infra.persistence.repositories.applications.mongo.daos;

import com.langa.backend.infra.persistence.repositories.applications.mongo.documents.ApplicationUsageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoApplicationUsageDao extends MongoRepository<ApplicationUsageDocument, String> {
}
