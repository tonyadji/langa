package com.langa.backend.domain.applications.usecases.fetch;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogQueryRepository;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.domainexchange.user.UserAccountService;

import java.util.Set;

@UseCase
public class GetLogUseCase {

    public static final String APPLICATION_NOT_FOUND_WITH_ID = "Application not found with id: ";
    private final LogQueryRepository logRepository;
    private final ApplicationRepository applicationRepository;
    private final UserAccountService userAccountService;

    public GetLogUseCase(LogQueryRepository logRepository, ApplicationRepository applicationRepository, UserAccountService userAccountService) {
        this.logRepository = logRepository;
        this.applicationRepository = applicationRepository;
        this.userAccountService = userAccountService;
    }

    public PaginatedResult<LogEntry> getFilteredLogs(
            String appId,
            String userEmail,
            LogFilter filter,
            int page,
            int size
    ) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException(APPLICATION_NOT_FOUND_WITH_ID + appId, null, Errors.APPLICATION_NOT_FOUND));

        Set<String> accountKeys = userAccountService.getAllAccountKeys(userEmail);
        app.authorizedToAccess(userEmail, accountKeys);

        PaginatedResult<LogEntry> pageResult = logRepository.findFiltered(app.getKey(), filter, page, size);

        return new PaginatedResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getPage(),
                pageResult.getSize()
        );
    }

}
