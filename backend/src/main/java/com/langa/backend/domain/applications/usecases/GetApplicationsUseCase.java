package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetApplicationsUseCase {

    private final ApplicationRepository applicationRepository;

    public List<ApplicationInfo> getApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toApplicationDto)
                .toList();
    }

    public List<ApplicationInfo> getApplications(String owner) {
        return applicationRepository.findByOwner(owner)
                .stream()
                .map(this::toApplicationDto)
                .toList();
    }

    private ApplicationInfo toApplicationDto(Application application) {
        return new ApplicationInfo(application.getId(), application.getName(), application.getKey(), application.getAccountKey(), application.getOwner());
    }

    public Application getApplication(String appId, String username) {
        return applicationRepository.findByIdAndOwner(appId, username)
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));
    }
}
