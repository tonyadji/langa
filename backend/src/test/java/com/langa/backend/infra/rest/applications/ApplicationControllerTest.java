package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.usecases.fetch.GetApplicationsUseCase;
import com.langa.backend.domain.applications.usecases.fetch.GetLogUseCase;
import com.langa.backend.domain.applications.usecases.fetch.GetMetricsUseCase;
import com.langa.backend.domain.applications.usecases.fetch.GetUsageUseCase;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.config.LangaApplicationProperties;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.ApplicationLogsResponseDto;
import com.langa.backend.infra.rest.applications.dto.ApplicationMetricsResponseDto;
import com.langa.backend.infra.rest.applications.dto.ApplicationUsageDto;
import com.langa.backend.infra.rest.applications.dto.LogFilterDto;
import com.langa.backend.infra.rest.applications.dto.MetricFilterDto;
import com.langa.backend.infra.rest.applications.dto.SecuredApplicationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private GetApplicationsUseCase getApplicationsUseCase;
    @Mock
    private GetLogUseCase getLogUseCase;
    @Mock
    private GetMetricsUseCase getMetricsUseCase;
    @Mock
    private GetUsageUseCase getUsageUseCase;

    private ApplicationController controller;
    private final UserDetails userDetails = new User("owner@example.com", "pw", List.of());

    @BeforeEach
    void setUp() {
        LangaApplicationProperties properties = new LangaApplicationProperties();
        properties.setEndpoint("/api/ingestion");
        properties.setKafkaUrl("localhost:9092");
        properties.setKafkaTopic("langa");
        controller = new ApplicationController(properties, getApplicationsUseCase, getLogUseCase, getMetricsUseCase, getUsageUseCase);
    }

    @Test
    void getAllApplications_shouldReturnMappedDtos() {
        ApplicationInfo info = new ApplicationInfo("id-1", "My App", "key-1", "acc-1", "owner@example.com", Collections.emptySet());
        when(getApplicationsUseCase.getApplications("owner@example.com")).thenReturn(List.of(info));

        ResponseEntity<List<ApplicationDto>> response = controller.getAllApplications(userDetails);

        assertEquals(1, response.getBody().size());
        assertEquals("My App", response.getBody().get(0).name());
    }

    @Test
    void getAllApplicationDetails_shouldReturnSecuredDto() {
        Application app = Application.createNew("My App", "acc-1", "owner@example.com");
        when(getApplicationsUseCase.getSecuredApplication("app-1", "owner@example.com")).thenReturn(app);

        ResponseEntity<SecuredApplicationDto> response = controller.getAllApplicationDetails(userDetails, "app-1");

        assertEquals(app.getSecret(), response.getBody().getSecret());
    }

    @Test
    void getApplicationDetails_shouldReturnDto() {
        Application app = Application.createNew("My App", "acc-1", "owner@example.com");
        when(getApplicationsUseCase.getApplication("app-1", "owner@example.com")).thenReturn(app);

        ResponseEntity<ApplicationDto> response = controller.getApplicationDetails(userDetails, "app-1");

        assertEquals("My App", response.getBody().name());
    }

    @Test
    void getFilteredLogsByAppId_shouldReturnPaginatedLogs() {
        Application app = Application.createNew("My App", "acc-1", "owner@example.com");
        when(getApplicationsUseCase.getApplication("app-1", "owner@example.com")).thenReturn(app);
        LogEntry log = new LogEntry().setMessage("hi").setLevel("INFO").setLoggerName("logger").setTimestamp(Instant.now());
        PaginatedResult<LogEntry> page = new PaginatedResult<>(List.of(log), 1, 1, 0, 20);
        when(getLogUseCase.getFilteredLogs(eq("app-1"), eq("owner@example.com"), any(), eq(0), eq(20)))
                .thenReturn(page);

        ResponseEntity<ApplicationLogsResponseDto> response = controller.getFilteredLogsByAppId(
                userDetails, "app-1", new LogFilterDto(null, null, null, null), 0, 20);

        assertEquals("My App", response.getBody().appName());
        assertEquals(1, response.getBody().paginatedLogs().getContent().size());
    }

    @Test
    void getFilteredMetrics_shouldReturnPaginatedMetrics() {
        Application app = Application.createNew("My App", "acc-1", "owner@example.com");
        when(getApplicationsUseCase.getApplication("app-1", "owner@example.com")).thenReturn(app);
        MetricEntry metric = new MetricEntry().setName("http.request").setStatus("SUCCESS").setTimestamp(Instant.now());
        PaginatedResult<MetricEntry> page = new PaginatedResult<>(List.of(metric), 1, 1, 0, 20);
        when(getMetricsUseCase.getFilteredMetrics(eq("app-1"), eq("owner@example.com"), any(), eq(0), eq(20)))
                .thenReturn(page);

        ResponseEntity<ApplicationMetricsResponseDto> response = controller.getFilteredMetrics(
                userDetails, "app-1", new MetricFilterDto(null, null, null, null, null, null, null, null, null, null), 0, 20);

        assertEquals("My App", response.getBody().appName());
        assertEquals(1, response.getBody().paginatedMetrics().getContent().size());
    }

    @Test
    void getUsage_shouldReturnUsageDto() {
        ApplicationUsageInfo usageInfo = new ApplicationUsageInfo("id-1", "key-1", "My App", 100L, 50L, Collections.emptyList());
        when(getUsageUseCase.getApplicationUsage("app-1", "owner@example.com")).thenReturn(usageInfo);

        ResponseEntity<ApplicationUsageDto> response = controller.getUsage(userDetails, "app-1");

        assertEquals(100L, response.getBody().logUsage());
    }
}
