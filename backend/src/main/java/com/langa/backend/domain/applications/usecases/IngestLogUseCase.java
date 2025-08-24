package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.infra.rest.applications.dto.LogRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngestLogUseCase {

    private final ApplicationRepository applicationRepository;
    private final LogEntryRepository logEntryRepository;

    public void process(LogRequestDto logRequestDto) {
        final Application application = applicationRepository
                .findByKeyAndAccountKey(logRequestDto.appKey(),
                        logRequestDto.accountKey())
                .orElseThrow(() -> new ApplicationException("Application not found with key: " + logRequestDto.appKey() + " and account key: " + logRequestDto.accountKey(), null, Errors.APPLICATION_NOT_FOUND));

        logEntryRepository.saveAll(application.createLogEntries(logRequestDto.logs()));
    }
}
