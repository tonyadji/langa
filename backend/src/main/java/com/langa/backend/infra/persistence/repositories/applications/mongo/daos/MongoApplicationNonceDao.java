package com.langa.backend.infra.persistence.repositories.applications.mongo.daos;

import com.langa.backend.infra.persistence.repositories.applications.mongo.documents.ApplicationNonceEntries;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoApplicationNonceDao extends MongoRepository<ApplicationNonceEntries, String> {
    boolean existsByAppKeyAndNonce(String appKey, String nonce);
}
