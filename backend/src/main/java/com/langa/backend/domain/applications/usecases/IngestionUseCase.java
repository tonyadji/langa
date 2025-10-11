package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.domain.applications.repositories.MetricEntryRepository;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class IngestionUseCase {

    private final ApplicationRepository applicationRepository;
    private final LogEntryRepository logEntryRepository;
    private final MetricEntryRepository metricEntryRepository;

    public void process(IngestionRequestDto ingestionRequestDto) {
        if (ingestionRequestDto instanceof LogIngestionRequestDto logIngestionRequestDto) {
            processLogIngestion(logIngestionRequestDto);
        } else if (ingestionRequestDto instanceof MetricIngestionRequestDto metricIngestionRequestDto){
            processMetricIngestion(metricIngestionRequestDto);
        }
    }

    private void processLogIngestion(LogIngestionRequestDto logRequestDto) {
        final Application application = applicationRepository
                .findByKeyAndAccountKey(logRequestDto.appKey(),
                        logRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + logRequestDto.appKey() + " and account key: " + logRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        logEntryRepository.saveAll(application.createLogEntries(logRequestDto.getEntries()));
    }

    private void processMetricIngestion(MetricIngestionRequestDto metricIngestionRequestDto) {
        final Application application = applicationRepository
                .findByKeyAndAccountKey(metricIngestionRequestDto.appKey(),
                        metricIngestionRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + metricIngestionRequestDto.appKey() + " and account key: " + metricIngestionRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        metricEntryRepository.saveAll(application.createMetricEntries(metricIngestionRequestDto.getEntries()));
    }
}
