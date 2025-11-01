package com.langa.backend.infra.adapters.persistence.logentries;

import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.LogEntryDocument;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.MongoLogEntryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LogEntryRepositoryImpl implements LogEntryRepository {

    private final MongoLogEntryDao mongoLogEntryDao;
    private final MongoTemplate mongoTemplate;

    @Override
    public LogEntry save(LogEntry logEntry) {
        return mongoLogEntryDao.save(LogEntryDocument.of(logEntry))
                .toLogEntry();
    }

    @Override
    public List<LogEntry> findByAppKeyOrderByTimestampDesc(String id) {
        return mongoLogEntryDao.findByAppKeyOrderByTimestampDesc(id)
                .stream()
                .map(LogEntryDocument::toLogEntry)
                .toList();
    }

    @Override
    public List<LogEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey) {
        return mongoLogEntryDao.findByAppKeyAndAccountKeyOrderByTimestampDesc(appKey, accountKey)
                .stream()
                .map(LogEntryDocument::toLogEntry)
                .toList();
    }

    @Override
    public List<LogEntry> saveAll(List<LogEntry> logs) {
        return mongoLogEntryDao.saveAll(logs.stream().map(LogEntryDocument::of).toList())
                .stream()
                .map(LogEntryDocument::toLogEntry)
                .toList();
    }

    @Override
    public PaginatedResult<LogEntry> findFiltered(String appKey, String accountKey, LogFilter filter, int page, int size) {

        Query query = new Query();

        query.addCriteria(Criteria.where("appKey").is(appKey));
        query.addCriteria(Criteria.where("accountKey").is(accountKey));

        if (filter.getLogLevel() != null && !filter.getLogLevel().isEmpty()) {
            query.addCriteria(Criteria.where("level").is(filter.getLogLevel()));
        }

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            query.addCriteria(Criteria.where("message").regex(".*" + filter.getKeyword() + ".*", "i"));
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

        List<LogEntryDocument> documents = mongoTemplate.find(query, LogEntryDocument.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), LogEntryDocument.class);

        List<LogEntry> content = documents.stream().map(LogEntryDocument::toLogEntry).toList();
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResult<>(content, total, totalPages, page, size);
    }
}
