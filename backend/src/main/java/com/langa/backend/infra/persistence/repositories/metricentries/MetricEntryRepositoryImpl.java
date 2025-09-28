package com.langa.backend.infra.persistence.repositories.metricentries;

import com.langa.backend.domain.applications.repositories.MetricEntryRepository;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.persistence.repositories.metricentries.mongo.MetricEntryDocument;
import com.langa.backend.infra.persistence.repositories.metricentries.mongo.MongoMetricEntryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MetricEntryRepositoryImpl implements MetricEntryRepository {

    private final MongoMetricEntryDao mongoMetricEntryDao;
    private final MongoTemplate mongoTemplate;

    @Override
    public MetricEntry save(MetricEntry metricEntry) {
        return mongoMetricEntryDao.save(MetricEntryDocument.of(metricEntry))
                .toMetricEntry();
    }

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
    public List<MetricEntry> saveAll(List<MetricEntry> metrics) {
        return mongoMetricEntryDao.saveAll(metrics.stream().map(MetricEntryDocument::of).toList())
                .stream()
                .map(MetricEntryDocument::toMetricEntry)
                .toList();
    }

    @Override
    public PaginatedResult<MetricEntry> findFiltered(String appKey, String accountKey, MetricFilter filter, int page, int size) {

        Query query = new Query();

        query.addCriteria(Criteria.where("appKey").is(appKey));
        query.addCriteria(Criteria.where("accountKey").is(accountKey));

        if(filter.getName() != null && !filter.getName().isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(".*" + filter.getName() + ".*", "i"));
        }

        if (filter.getDurationGreaterThan() != null && filter.getDurationGreaterThan() > 0) {
            query.addCriteria(Criteria.where("durationMillis").gt(filter.getDurationGreaterThan()));
        }

        if (filter.getDurationLessThan() != null && filter.getDurationLessThan() > 0) {
            query.addCriteria(Criteria.where("durationMillis").lt(filter.getDurationLessThan()));
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

        if (filter.getHttpStatus() > 0) {
            query.addCriteria(Criteria.where("httpStatus").is(filter.getHttpStatus()));
        }

        if (filter.getStartDate() != null) {
            query.addCriteria(Criteria.where("timestamp").gte(filter.getStartDate()));
        }

        if (filter.getEndDate() != null) {
            query.addCriteria(Criteria.where("timestamp").lt(filter.getEndDate()));
        }

        int skip = page * size;
        query.skip(skip).limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        List<MetricEntryDocument> documents = mongoTemplate.find(query, MetricEntryDocument.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), MetricEntryDocument.class);
        List<MetricEntry> content = documents.stream().map(MetricEntryDocument::toMetricEntry).toList();
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResult<>(content, total, totalPages, page, size);
    }
}
