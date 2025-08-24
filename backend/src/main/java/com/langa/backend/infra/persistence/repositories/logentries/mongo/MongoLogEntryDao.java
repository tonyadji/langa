package com.langa.backend.infra.persistence.repositories.logentries.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoLogEntryDao extends MongoRepository<LogEntryDocument, String> {

    List<LogEntryDocument> findByAppKeyOrderByTimestampDesc(String appKey);

    List<LogEntryDocument> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);
}
