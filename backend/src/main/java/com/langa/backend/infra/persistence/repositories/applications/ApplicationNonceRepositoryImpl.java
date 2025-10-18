package com.langa.backend.infra.persistence.repositories.applications;

import com.langa.backend.domain.applications.repositories.ApplicationNonceRepository;
import com.langa.backend.infra.persistence.repositories.applications.mongo.daos.MongoApplicationNonceDao;
import com.langa.backend.infra.persistence.repositories.applications.mongo.documents.ApplicationNonceEntries;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class ApplicationNonceRepositoryImpl implements ApplicationNonceRepository {

    private final MongoApplicationNonceDao mongoApplicationNonceDao;

    public ApplicationNonceRepositoryImpl(MongoApplicationNonceDao mongoApplicationNonceDao) {
        this.mongoApplicationNonceDao = mongoApplicationNonceDao;
    }

    @Override
    public boolean existsByAppKeyAndNonce(String appKey, String nonce) {
        return mongoApplicationNonceDao.existsByAppKeyAndNonce(appKey, nonce);
    }

    @Override
    public void save(String appKey, String nonce, LocalDateTime usageDate) {
        mongoApplicationNonceDao.save(new ApplicationNonceEntries(appKey, nonce, usageDate));
    }
}
