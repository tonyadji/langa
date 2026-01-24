package com.langa.backend.domain.applications.usecases.fetch;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.MetricQueryRepository;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.domainexchange.user.UserAccountService;

import java.util.Set;

@UseCase
public class GetMetricsUseCase {

    private final ApplicationRepository applicationRepository;
    private final MetricQueryRepository metricQueryRepository;
    private final UserAccountService userAccountService;

    public GetMetricsUseCase(ApplicationRepository applicationRepository, MetricQueryRepository metricQueryRepository, UserAccountService userAccountService) {
        this.applicationRepository = applicationRepository;
        this.metricQueryRepository = metricQueryRepository;
        this.userAccountService = userAccountService;
    }

    public PaginatedResult<MetricEntry> getFilteredMetrics (String appId,
                                                            String userEmail,
                                                            MetricFilter filter,
                                                            int page,
                                                            int size) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException("Application not found with id: " + appId, null, Errors.APPLICATION_NOT_FOUND));

        Set<String> accountKeys = userAccountService.getAllAccountKeys(userEmail);
        app.authorizedToAccess(userEmail, accountKeys);

        PaginatedResult<MetricEntry> pageResult = metricQueryRepository.findFiltered(app.getKey(), app.getAccountKey(), filter, page, size);

        return new PaginatedResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getPage(),
                pageResult.getSize()
        );
    }
}
