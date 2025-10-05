package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;

@UseCase
public class CreateApplicationUseCase {

    private final ApplicationRepository applicationRepository;
    private final UserAccountService userAccountService;

    public CreateApplicationUseCase(ApplicationRepository applicationRepository,
                                    UserAccountService userAccountService) {
        this.applicationRepository = applicationRepository;
        this.userAccountService = userAccountService;
    }

    public ApplicationInfo create(String name, String ownerEmail) {
        applicationRepository.findByOwnerAndName(ownerEmail, name)
                .ifPresent(app -> {
                    throw new ApplicationException(
                            "Application name already exists",
                            null,
                            Errors.APPLICATION_NAME_ALREADY_EXISTS
                    );
                });

        String accountKey = userAccountService.getAccountKey(ownerEmail);

        Application app = Application.createNew(name, accountKey, ownerEmail);

        return ApplicationInfo.of(applicationRepository.save(app));
    }
}
