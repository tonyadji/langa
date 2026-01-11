package com.langa.backend.infra.adapters.persistence.applications.mongo.daos;

import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationUsageTrendDocument;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.UsageSumDto;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoApplicationUsageTrendDao extends MongoRepository<ApplicationUsageTrendDocument, String> {
    List<ApplicationUsageTrendDocument> findByAppKeyOrderByCreatedDateDesc(String appKey);

    @Aggregation(pipeline = {
            "{ '$match': { 'appKey': ?0, 'type': ?1 } }",

            "{ '$group': { '_id': null, 'total': { '$sum': '$usage' } } }"
    })
    Optional<UsageSumDto> sumUsageByAppKeyAndType(String appKey, IngestionType type);
}
