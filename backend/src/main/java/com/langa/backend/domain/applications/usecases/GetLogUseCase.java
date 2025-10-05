package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

import java.util.List;

@UseCase
public class GetLogUseCase {

    public static final String APPLICATION_NOT_FOUND_WITH_ID = "Application not found with id: ";
    private final LogEntryRepository logRepository;
    private final ApplicationRepository applicationRepository;

    public GetLogUseCase(LogEntryRepository logRepository, ApplicationRepository applicationRepository) {
        this.logRepository = logRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<LogEntry> getLogs(String appId) {
        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException(APPLICATION_NOT_FOUND_WITH_ID + appId, null, Errors.APPLICATION_NOT_FOUND));

        return logRepository.findByAppKeyAndAccountKeyOrderByTimestampDesc(app.getKey(), app.getAccountKey())
                .stream()
                .toList();
    }

    public List<LogEntry> getLogs(String appId, String username) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException(APPLICATION_NOT_FOUND_WITH_ID + appId, null, Errors.APPLICATION_NOT_FOUND));

        app.checkOwnership(username);

        return logRepository.findByAppKeyAndAccountKeyOrderByTimestampDesc(app.getKey(), app.getAccountKey())
                .stream()
                .toList();
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

        app.checkOwnership(userEmail);


        PaginatedResult<LogEntry> pageResult = logRepository.findFiltered(app.getKey(), app.getAccountKey(), filter, page, size);

        return new PaginatedResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getPage(),
                pageResult.getSize()
        );
    }

}
