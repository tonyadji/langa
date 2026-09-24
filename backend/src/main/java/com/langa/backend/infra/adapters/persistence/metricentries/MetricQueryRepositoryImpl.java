package com.langa.backend.infra.adapters.persistence.metricentries;

import java.util.regex.Pattern;
import com.langa.backend.domain.applications.repositories.MetricQueryRepository;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MetricEntryDocument;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MongoMetricEntryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MetricQueryRepositoryImpl implements MetricQueryRepository {

    public static final String TIMESTAMP = "timestamp";
    private final MongoMetricEntryDao mongoMetricEntryDao;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<MetricEntry> findByAppKeyOrderByTimestampDesc(String id) {
        return mongoMetricEntryDao.findByAppKeyOrderByTimestampDesc(id)
                .stream()
                .map(MetricEntryDocument::toMetricEntry)
                .toList();
    }

    @Override
    public List<MetricEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey) {
        return mongoMetricEntryDao.findByAppKeyAndAccountKeyOrderByTimestampDesc(appKey, accountKey)
                .stream()
                .map(MetricEntryDocument::toMetricEntry)
                .toList();
    }

    @Override
    public PaginatedResult<MetricEntry> findFiltered(String appKey, String accountKey, MetricFilter filter, int page, int size) {

        Query query = new Query();

        query.addCriteria(Criteria.where("appKey").is(appKey));
        query.addCriteria(Criteria.where("accountKey").is(accountKey));

        setNameCriteria(filter, query);

        boolean hasDurationGreaterThan = filter.getDurationGreaterThan() != null && filter.getDurationGreaterThan() > 0;
        boolean hasDurationLessThan = filter.getDurationLessThan() != null && filter.getDurationLessThan() > 0;
        if (hasDurationGreaterThan || hasDurationLessThan) {
            Criteria durationCriteria = Criteria.where("durationMillis");
            if (hasDurationGreaterThan) {
                durationCriteria = durationCriteria.gt(filter.getDurationGreaterThan());
            }
            if (hasDurationLessThan) {
                durationCriteria = durationCriteria.lt(filter.getDurationLessThan());
            }
            query.addCriteria(durationCriteria);
        }

        if (filter.getHttpMethod() != null && !filter.getHttpMethod().isEmpty()) {
            query.addCriteria(Criteria.where("httpMethod").is(filter.getHttpMethod()));
        }

        if (filter.getUri() != null && !filter.getUri().isEmpty()) {
            query.addCriteria(Criteria.where("uri").is(filter.getUri()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(filter.getStatus()));
        }

        if (filter.getHttpStatus() != null && filter.getHttpStatus() > 0) {
            query.addCriteria(Criteria.where("httpStatus").is(filter.getHttpStatus()));
        }

        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            Criteria timestampCriteria = Criteria.where(TIMESTAMP);
            if (filter.getStartDate() != null) {
                timestampCriteria = timestampCriteria.gte(filter.getStartDate());
            }
            if (filter.getEndDate() != null) {
                timestampCriteria = timestampCriteria.lt(filter.getEndDate());
            }
            query.addCriteria(timestampCriteria);
        }

        int skip = page * size;
        query.skip(skip).limit(size);
        query.with(Sort.by(Sort.Direction.DESC, TIMESTAMP));

        List<MetricEntryDocument> documents = mongoTemplate.find(query, MetricEntryDocument.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), MetricEntryDocument.class);
        List<MetricEntry> content = documents.stream().map(MetricEntryDocument::toMetricEntry).toList();
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResult<>(content, total, totalPages, page, size);
    }

    private static void setNameCriteria(MetricFilter filter, Query query) {
        if(filter.getName() != null && !filter.getName().isEmpty()) {
            // The name is matched literally: user input must never be interpreted as a regular expression
            query.addCriteria(Criteria.where("name").regex(Pattern.quote(filter.getName()), "i"));
        }
    }

}
