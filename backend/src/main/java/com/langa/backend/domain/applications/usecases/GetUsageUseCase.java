package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.ApplicationUsageRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;

@UseCase
public class GetUsageUseCase {

    private final ApplicationRepository applicationRepository;
    private final ApplicationUsageRepository applicationUsageRepository;

    public GetUsageUseCase(ApplicationRepository applicationRepository, ApplicationUsageRepository applicationUsageRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationUsageRepository = applicationUsageRepository;
    }


    public ApplicationUsageInfo getApplicationUsage(String appId, String username) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException("Application not found with id: " + appId, null, Errors.APPLICATION_NOT_FOUND));

        app.checkOwnership(username);

        return applicationUsageRepository.findByApplicationKey(app.getKey())
                .map(usage -> new ApplicationUsageInfo(
                        app.getId(),
                        app.getKey(),
                        app.getName(),
                        usage.totalLogBytes(),
                        usage.totalMetricBytes()
                )).orElse(new ApplicationUsageInfo(
                        app.getId(),
                        app.getKey(),
                        app.getName(),
                        0,
                        0));
    }
}
