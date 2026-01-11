package com.langa.backend.infra.adapters.persistence.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsage;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageTrend;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoApplicationDao;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoApplicationUsageTrendDao;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationDocument;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationUsageTrendDocument;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.UsageSumDto;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.LogEntryDocument;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.MongoLogEntryDao;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MetricEntryDocument;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MongoMetricEntryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private final MongoApplicationDao mongoApplicationDao;
    private final MongoLogEntryDao mongoLogEntryDao;
    private final MongoMetricEntryDao mongoMetricEntryDao;
    private final MongoApplicationUsageTrendDao mongoApplicationUsageTrendDao;
    private final MongoTemplate mongoTemplate;

    @Override
    public Application save(Application application) {

        ApplicationDocument applicationDocument = ApplicationDocument.of(application);
        if (!application.getNewLogEntries().isEmpty()) {
            List<LogEntryDocument> logEntryDocuments = application.getNewLogEntries().stream().map(LogEntryDocument::of).toList();
            mongoLogEntryDao.saveAll(logEntryDocuments);
            mongoApplicationUsageTrendDao.save(ApplicationUsageTrendDocument.ofLog(application));
        }

        if (!application.getNewMetricsEntries().isEmpty()) {
            List<MetricEntryDocument> metricEntryDocuments = application.getNewMetricsEntries().stream().map(MetricEntryDocument::of).toList();
            mongoMetricEntryDao.saveAll(metricEntryDocuments);

            mongoApplicationUsageTrendDao.save(ApplicationUsageTrendDocument.ofMetric(application));
        }

        long usageLog = mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(application.getKey(), IngestionType.LOG).map(UsageSumDto::total).orElse(0L);
        long usageMetric = mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(application.getKey(), IngestionType.METRIC).map(UsageSumDto::total).orElse(0L);
        applicationDocument.setUsage(new ApplicationUsage(usageLog, usageMetric, Instant.now()));

        mongoApplicationDao.save(applicationDocument);
        log.debug("AFTER : application usage logs : {}",applicationDocument.getUsage().totalLogBytes());
        log.debug("AFTER : application usage metrics : {}",applicationDocument.getUsage().totalMetricBytes());

        return applicationDocument.toApplication();
    }

    @Override
    public Application findByKey(String key) {
        return mongoApplicationDao.findByKey(key)
                .map(ApplicationDocument::toApplication)
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + key, null, Errors.APPLICATION_NOT_FOUND));
    }

    @Override
    public long count() {
        return mongoApplicationDao.count();
    }

    @Override
    public List<Application> findAll() {
        return mongoApplicationDao.findAll().stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public Optional<Application> findById(String id) {
        return mongoApplicationDao.findById(id)
                .map(ApplicationDocument::toApplication)
                .or(Optional::empty);
    }

    @Override
    public Optional<Application> findByKeyAndAccountKey(String key, String accountKey) {
        return mongoApplicationDao.findByKeyAndAccountKey(key, accountKey)
                .map(ApplicationDocument::toApplication)
                .or(Optional::empty);
    }

    @Override
    public List<Application> findByAccountKey(String accountKey) {
        return mongoApplicationDao.findByAccountKey(accountKey)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public List<Application> findByOwner(String owner) {
        return mongoApplicationDao.findByOwner(owner)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public Optional<Application> findByOwnerAndName(String owner, String name) {
        return mongoApplicationDao.findByOwnerAndName(owner, name)
                .map(ApplicationDocument::toApplication)
                .or(Optional::empty);
    }

    @Override
    public Optional<Application> findByIdAndOwner(String appId, String username) {
        return mongoApplicationDao.findByIdAndOwner(appId, username)
                .map(ApplicationDocument::toApplication)
                .or(Optional::empty);
    }

    @Override
    public Optional<Application> securedFindByIdAndOwner(String appId, String username) {
        return mongoApplicationDao.findByIdAndOwner(appId, username)
                .map(ApplicationDocument::toSecuredApplication)
                .or(Optional::empty);
    }

    @Override
    public List<ApplicationUsageTrend> findApplicationUsageTrends(String key) {
        return mongoApplicationUsageTrendDao.findByAppKeyOrderByCreatedDateDesc(key)
                .stream().map(ApplicationUsageTrendDocument::toApplicationTrend)
                .toList();
    }

    @Override
    public List<Application> findBySharedWithUser(String sharedWith) {
        return mongoApplicationDao.findBySharedWith_KeyAndSharedWith_ExpirationDateIsNullAndSharedWith_RevokedDateIsNull(sharedWith)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public List<Application> findBySharedWithTeams(Set<String> teamKeys) {
        return mongoApplicationDao.findBySharedWith_KeyInAndSharedWith_ExpirationDateIsNullAndSharedWith_RevokedDateIsNull(teamKeys)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public Optional<Application> findSecuredAppByKeyAndAccountKey(String key, String accountKey) {
        return mongoApplicationDao.findByKeyAndAccountKey(key, accountKey)
                .map(ApplicationDocument::toSecuredApplication)
                .or(Optional::empty);
    }
}
