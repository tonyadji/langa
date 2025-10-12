package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.ApplicationUsage;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.ApplicationUsageRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.domain.applications.repositories.MetricEntryRepository;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class IngestionUseCase {

    private final ApplicationRepository applicationRepository;
    private final LogEntryRepository logEntryRepository;
    private final MetricEntryRepository metricEntryRepository;
    private final IngestionSizeCalculator ingestionSizeCalculator;
    private final ApplicationUsageRepository applicationUsageRepository;
    private final IngestionSecurity ingestionSecurity;

    public void process(IngestionRequestDto ingestionRequestDto, IngestionCredentials ingestionCredentials) {
        if (ingestionRequestDto instanceof LogIngestionRequestDto logIngestionRequestDto) {
            processLogIngestion(logIngestionRequestDto, ingestionCredentials);
        } else if (ingestionRequestDto instanceof MetricIngestionRequestDto metricIngestionRequestDto) {
            processMetricIngestion(metricIngestionRequestDto, ingestionCredentials);
        }
    }

    private void processLogIngestion(LogIngestionRequestDto logRequestDto, IngestionCredentials credentials) {
        final Application application = applicationRepository
                .findByKeyAndAccountKey(logRequestDto.appKey(),
                        logRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + logRequestDto.appKey() + " and account key: " + logRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        if (!ingestionSecurity.isAuthorized(credentials, application)) {
            throw new ApplicationException("Ingestion unauthorized", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        List<LogEntry> logEntries = application.createLogEntries(logRequestDto.getEntries());
        logEntryRepository.saveAll(logEntries);
        updateApplicationUsage(application.getKey(), ingestionSizeCalculator.calculateSizeInBytes(logEntries), IngestionType.LOG);
    }

    private void processMetricIngestion(MetricIngestionRequestDto metricIngestionRequestDto, IngestionCredentials credentials) {
        final Application application = applicationRepository
                .findByKeyAndAccountKey(metricIngestionRequestDto.appKey(),
                        metricIngestionRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + metricIngestionRequestDto.appKey() + " and account key: " + metricIngestionRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        if (!ingestionSecurity.isAuthorized(credentials, application)) {
            throw new ApplicationException("Ingestion unauthorized", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        List<MetricEntry> metricEntries = application.createMetricEntries(metricIngestionRequestDto.getEntries());
        metricEntryRepository.saveAll(metricEntries);
        updateApplicationUsage(application.getKey(), ingestionSizeCalculator.calculateSizeInBytes(metricEntries), IngestionType.METRIC);
    }

    private void updateApplicationUsage(String appKey, long bytes, IngestionType ingestionType) {
        ApplicationUsage usage = applicationUsageRepository.findByApplicationKey(appKey)
                .orElseGet(() -> new ApplicationUsage(appKey, 0L, 0L));

        if (ingestionType == IngestionType.LOG) {
            usage = usage.increaseLogBytes(bytes);
        } else {
            usage = usage.increaseTotalMetricBytes(bytes);
        }
        applicationUsageRepository.save(usage);
    }
}
