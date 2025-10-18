package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@UseCase
@RequiredArgsConstructor
public class GetApplicationsUseCase {

    private final ApplicationRepository applicationRepository;
    private final UserAccountService userAccountService;

    public List<ApplicationInfo> getApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toOwnedApplicationDto)
                .toList();
    }

    public List<ApplicationInfo> getApplications(String owner) {
        final String accountKey = userAccountService.getAccountKey(owner);
        final Set<String> teamKeys = userAccountService.getTeamKeys(owner);
        final Stream<ApplicationInfo> ownedApplications = applicationRepository.findByOwner(owner)
                .stream()
                .map(this::toOwnedApplicationDto);
        final Stream<ApplicationInfo> shareApplications = applicationRepository.findBySharedWithUser(accountKey)
                .stream()
                .map(this::toSharedApplicationDto);

        final Stream<ApplicationInfo> teamApplications = applicationRepository.findBySharedWithTeams(teamKeys)
                .stream()
                .map(this::toSharedApplicationDto);
        return Stream.of(ownedApplications, shareApplications, teamApplications)
                .flatMap(stream -> stream)
                .distinct()
                .toList();
    }

    public Application getApplication(String appId, String username) {
        return applicationRepository.findByIdAndOwner(appId, username)
                .or(() -> applicationRepository.findById(appId))
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));
    }

    private ApplicationInfo toOwnedApplicationDto(Application application) {
        return new ApplicationInfo(application.getId(),
                application.getName(),
                application.getKey(),
                application.getAccountKey(),
                application.getOwner(),
                application.getSharedWith());
    }

    private ApplicationInfo toSharedApplicationDto(Application application) {
        return new ApplicationInfo(application.getId(),
                application.getName(),
                null,
                null,
                application.getOwner(),
                null);
    }
}
