package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.Application;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ApplicationRepository {

    Application save(Application application);
    Application findByKey(String key);

    long count();

    List<Application> findAll();

    Optional<Application> findById(String id);

    Optional<Application> findByKeyAndAccountKey(String key, String accountKey);

    List<Application> findByAccountKey(String accountKey);

    List<Application> findByOwner(String owner);

    Optional<Application> findByOwnerAndName(String owner, String name);

    Optional<Application> findByIdAndOwner(String appId, String username);

    List<Application> findBySharedWithUser(String sharedWith);
    List<Application> findBySharedWithTeams(Set<String> teamKeys);
}
