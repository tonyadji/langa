package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.MetricEntryRepository;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

@UseCase
public class GetMetricsUseCase {

    private final ApplicationRepository applicationRepository;
    private final MetricEntryRepository metricEntryRepository;

    public GetMetricsUseCase(ApplicationRepository applicationRepository, MetricEntryRepository metricEntryRepository) {
        this.applicationRepository = applicationRepository;
        this.metricEntryRepository = metricEntryRepository;
    }

    public PaginatedResult<MetricEntry> getFilteredMetrics (String appId,
                                                            String userEmail,
                                                            MetricFilter filter,
                                                            int page,
                                                            int size) {
        final Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException("Application not found with id: " + appId, null, Errors.APPLICATION_NOT_FOUND));

        app.checkOwnership(userEmail);

        PaginatedResult<MetricEntry> pageResult = metricEntryRepository.findFiltered(app.getKey(), app.getAccountKey(), filter, page, size);

        return new PaginatedResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getPage(),
                pageResult.getSize()
        );
    }
}
