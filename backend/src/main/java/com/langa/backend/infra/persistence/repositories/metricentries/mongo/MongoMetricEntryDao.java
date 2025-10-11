package com.langa.backend.infra.persistence.repositories.metricentries.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoMetricEntryDao extends MongoRepository<MetricEntryDocument, String> {

    List<MetricEntryDocument> findByAppKeyOrderByTimestampDesc(String appKey);

    List<MetricEntryDocument> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);
}
