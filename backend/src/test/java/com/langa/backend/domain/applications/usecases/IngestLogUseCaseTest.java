package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.infra.rest.applications.dto.LogDto;
import com.langa.backend.infra.rest.applications.dto.LogRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestLogUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private LogEntryRepository logEntryRepository;

    @InjectMocks
    private IngestLogUseCase ingestLogUseCase;

    private Application app;

    @BeforeEach
    void setUp() {
        app = new Application()
                .setId("appId")
                .setKey("appKey")
                .setAccountKey("accountKey")
                .setOwner("owner@example.com");
    }

    @Test
    void process_shouldSaveLogs() {
        LogDto logDto = new LogDto("msg", "INFO", "logger", "2025-08-23T12:00:00");
        LogRequestDto requestDto = new LogRequestDto("appKey", "accountKey", List.of(logDto));

        when(applicationRepository.findByKeyAndAccountKey("appKey", "accountKey"))
                .thenReturn(Optional.of(app));

        ingestLogUseCase.process(requestDto);

        verify(logEntryRepository, times(1)).saveAll(app.createLogEntries(requestDto.logs()));
    }

    @Test
    void process_appNotFound_shouldThrowException() {
        LogRequestDto requestDto = new LogRequestDto("wrongKey", "wrongAccount", List.of());

        when(applicationRepository.findByKeyAndAccountKey("wrongKey", "wrongAccount"))
                .thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> ingestLogUseCase.process(requestDto));
        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }
}