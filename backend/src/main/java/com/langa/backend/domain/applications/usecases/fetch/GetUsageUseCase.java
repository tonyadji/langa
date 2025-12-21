package com.langa.backend.domain.applications.usecases.fetch;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;

@UseCase
public class GetUsageUseCase {

    private final ApplicationRepository applicationRepository;

    public GetUsageUseCase(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public ApplicationUsageInfo getApplicationUsage(String appId, String username) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException("Application not found with id: " + appId, null, Errors.APPLICATION_NOT_FOUND));

        app.checkOwnership(username);

        return new ApplicationUsageInfo(app.getId(), app.getKey(), app.getName(),
                app.getUsage().totalLogBytes(), app.getUsage().totalMetricBytes());
    }
}
