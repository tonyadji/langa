package com.langa.backend.infra.persistence.repositories.applications;

import com.langa.backend.domain.applications.ApplicationUsage;
import com.langa.backend.domain.applications.repositories.ApplicationUsageRepository;
import com.langa.backend.infra.persistence.repositories.applications.mongo.daos.MongoApplicationUsageDao;
import com.langa.backend.infra.persistence.repositories.applications.mongo.documents.ApplicationUsageDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApplicationUsageRepositoryImpl implements ApplicationUsageRepository {
    private final MongoApplicationUsageDao mongoApplicationUsageDao;

    public ApplicationUsageRepositoryImpl(MongoApplicationUsageDao mongoApplicationUsageDao) {
        this.mongoApplicationUsageDao = mongoApplicationUsageDao;
    }

    @Override
    public Optional<ApplicationUsage> findByApplicationKey(String applicationKey) {
        return mongoApplicationUsageDao.findByAppKey(applicationKey)
                .map(ApplicationUsageDocument::toApplicationUsage);
    }

    @Override
    public void save(ApplicationUsage usage) {
        mongoApplicationUsageDao.save(ApplicationUsageDocument.of(usage));
    }
}
