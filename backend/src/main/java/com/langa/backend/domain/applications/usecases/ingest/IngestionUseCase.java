package com.langa.backend.domain.applications.usecases.ingest;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class IngestionUseCase {

    private final ApplicationRepository applicationRepository;
    private final IngestionSizeCalculator ingestionSizeCalculator;
    private final IngestionSecurity ingestionSecurity;

    public void process(IngestionRequestDto ingestionRequestDto, IngestionCredentials ingestionCredentials) {
        log.info("Processing ingestion request: {}", ingestionRequestDto);
        if (ingestionRequestDto instanceof LogIngestionRequestDto logIngestionRequestDto) {
            processLogIngestion(logIngestionRequestDto, ingestionCredentials);
        } else if (ingestionRequestDto instanceof MetricIngestionRequestDto metricIngestionRequestDto) {
            processMetricIngestion(metricIngestionRequestDto, ingestionCredentials);
        }
    }

    private void processLogIngestion(LogIngestionRequestDto logRequestDto, IngestionCredentials credentials) {
        final Application application = applicationRepository
                .findSecuredAppByKeyAndAccountKey(logRequestDto.appKey(),
                        logRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + logRequestDto.appKey() + " and account key: " + logRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));
        if (!ingestionSecurity.isAuthorized(credentials, application)) {
            throw new ApplicationException("Ingestion unauthorized", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        application.createLogEntries(logRequestDto.getEntries(), ingestionSizeCalculator);
        applicationRepository.save(application);
    }

    private void processMetricIngestion(MetricIngestionRequestDto metricIngestionRequestDto, IngestionCredentials credentials) {
        final Application application = applicationRepository
                .findSecuredAppByKeyAndAccountKey(metricIngestionRequestDto.appKey(),
                        metricIngestionRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + metricIngestionRequestDto.appKey() + " and account key: " + metricIngestionRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        if (!ingestionSecurity.isAuthorized(credentials, application)) {
            throw new ApplicationException("Ingestion unauthorized", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        application.createMetricEntries(metricIngestionRequestDto.getEntries(), ingestionSizeCalculator);
        applicationRepository.save(application);
    }
}
