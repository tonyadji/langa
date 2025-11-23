package com.langa.backend.infra.adapters.persistence.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationDocument;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoApplicationDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private final MongoApplicationDao mongoApplicationDao;

    @Override
    public Application save(Application application) {
        final ApplicationDocument applicationDocument = ApplicationDocument.of(application);
        mongoApplicationDao.save(applicationDocument);
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
    public List<Application> findBySharedWithUser(String sharedWith) {
        return mongoApplicationDao.findBySharedWith_key(sharedWith)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public List<Application> findBySharedWithTeams(Set<String> teamKeys) {
        return mongoApplicationDao.findBySharedWith_KeyIn(teamKeys)
                .stream().map(ApplicationDocument::toApplication).toList();
    }

    @Override
    public Optional<Application> findSecuredAppByKeyAndAccountKey(String key, String accountKey) {
        return mongoApplicationDao.findByKeyAndAccountKey(key, accountKey)
                .map(ApplicationDocument::toSecuredApplication)
                .or(Optional::empty);
    }

}
