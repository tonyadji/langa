package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.ApplicationUsage;

import java.util.Optional;

public interface ApplicationUsageRepository {
    Optional<ApplicationUsage> findByApplicationKey(String applicationKey);
    void save(ApplicationUsage usage);
}
