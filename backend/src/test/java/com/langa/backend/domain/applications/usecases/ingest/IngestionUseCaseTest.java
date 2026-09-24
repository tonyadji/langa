package com.langa.backend.domain.applications.usecases.ingest;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.rest.common.dto.LogDto;
import com.langa.backend.infra.rest.common.dto.MetricDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private IngestionSizeCalculator ingestionSizeCalculator;
    @Mock
    private IngestionSecurity ingestionSecurity;

    @InjectMocks
    private IngestionUseCase useCase;

    private final IngestionCredentials credentials = new IngestionCredentials(
            "agent", "app-key", "acc-key", "123", "nonce:sig", IngestionSecurity.CredentialType.HTTP);

    @Test
    void process_shouldIngestLogs_whenAuthorized() {
        Application app = Application.createNew("My App", "acc-key", "owner@example.com");
        LogDto logDto = new LogDto("hello", "INFO", "logger", Instant.now().toString(), null, null, null);
        LogIngestionRequestDto dto = new LogIngestionRequestDto("app-key", "acc-key", List.of(logDto), IngestionType.LOG);

        when(applicationRepository.findSecuredAppByKeyAndAccountKey("app-key", "acc-key")).thenReturn(Optional.of(app));
        when(ingestionSecurity.isAuthorized(credentials, app)).thenReturn(true);
        when(ingestionSizeCalculator.calculateSizeInBytes(any())).thenReturn(10L);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.process(dto, credentials);

        verify(applicationRepository).save(app);
        assertEquals(10L, app.getUsage().totalLogBytes());
    }

    @Test
    void process_shouldThrow_whenApplicationNotFound() {
        LogIngestionRequestDto dto = new LogIngestionRequestDto("app-key", "acc-key", List.of(), IngestionType.LOG);
        when(applicationRepository.findSecuredAppByKeyAndAccountKey("app-key", "acc-key")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class, () -> useCase.process(dto, credentials));
        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }

    @Test
    void process_shouldThrow_whenNotAuthorized() {
        Application app = Application.createNew("My App", "acc-key", "owner@example.com");
        LogIngestionRequestDto dto = new LogIngestionRequestDto("app-key", "acc-key", List.of(), IngestionType.LOG);

        when(applicationRepository.findSecuredAppByKeyAndAccountKey("app-key", "acc-key")).thenReturn(Optional.of(app));
        when(ingestionSecurity.isAuthorized(credentials, app)).thenReturn(false);

        ApplicationException ex = assertThrows(ApplicationException.class, () -> useCase.process(dto, credentials));
        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void process_shouldIngestMetrics_whenAuthorized() {
        Application app = Application.createNew("My App", "acc-key", "owner@example.com");
        MetricDto metricDto = new MetricDto("http.request", null, 100, "SUCCESS", Instant.now().toString(), "/api", "GET", 200);
        MetricIngestionRequestDto dto = new MetricIngestionRequestDto("app-key", "acc-key", List.of(metricDto), IngestionType.METRIC);

        when(applicationRepository.findSecuredAppByKeyAndAccountKey("app-key", "acc-key")).thenReturn(Optional.of(app));
        when(ingestionSecurity.isAuthorized(credentials, app)).thenReturn(true);
        when(ingestionSizeCalculator.calculateSizeInBytes(any())).thenReturn(20L);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.process(dto, credentials);

        assertEquals(20L, app.getUsage().totalMetricBytes());
    }

    @Test
    void process_shouldThrow_whenMetricApplicationNotFound() {
        MetricIngestionRequestDto dto = new MetricIngestionRequestDto("app-key", "acc-key", List.of(), IngestionType.METRIC);
        when(applicationRepository.findSecuredAppByKeyAndAccountKey("app-key", "acc-key")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> useCase.process(dto, credentials));
    }
}
