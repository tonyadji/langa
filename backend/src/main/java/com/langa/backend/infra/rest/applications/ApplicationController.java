package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.usecases.*;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.config.LangaApplicationProperties;
import com.langa.backend.infra.rest.applications.dto.*;
import com.langa.backend.infra.rest.common.dto.LogDto;
import com.langa.backend.infra.rest.common.dto.MetricDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final LangaApplicationProperties applicationProperties;
    private final GetApplicationsUseCase getApplicationsUseCase;
    private final GetLogUseCase getLogUseCase;
    private final GetMetricsUseCase getMetricsUseCase;
    private final CreateApplicationUseCase createApplicationUseCase;
    private final GetUsageUseCase getUsageUseCase;

    public ApplicationController(LangaApplicationProperties applicationProperties, GetApplicationsUseCase getApplicationsUseCase, GetLogUseCase getLogUseCase, GetMetricsUseCase getMetricsUseCase, CreateApplicationUseCase createApplicationUseCase, GetUsageUseCase getUsageUseCase) {
        this.applicationProperties = applicationProperties;
        this.getApplicationsUseCase = getApplicationsUseCase;
        this.getLogUseCase = getLogUseCase;
        this.getMetricsUseCase = getMetricsUseCase;
        this.createApplicationUseCase = createApplicationUseCase;
        this.getUsageUseCase = getUsageUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationDto> createApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody CreateApplicationRequestDto applicationRequestDto) {

        return ResponseEntity.ok(ApplicationDto.of(createApplicationUseCase.create(applicationRequestDto.name(), userDetails.getUsername())));
    }

    @GetMapping()
    public ResponseEntity<List<ApplicationDto>> getAllApplications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(getApplicationsUseCase.getApplications(userDetails.getUsername())
                .stream().map(ApplicationDto::of).toList());
    }

    @GetMapping("{appId}/secured-details")
    public ResponseEntity<SecuredApplicationDto> getAllApplications(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String appId) {
        return ResponseEntity.ok(
                SecuredApplicationDto.of(
                        getApplicationsUseCase.getSecuredApplication(appId, userDetails.getUsername()),
                        applicationProperties.getHttpPrefix(),
                        applicationProperties.getKafkaPrefix()));
    }

    public ResponseEntity<List<LogDto>> getLogsByAppKey(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String appId) {
        return ResponseEntity.ok(getLogUseCase.getLogs(appId, userDetails.getUsername())
                .stream().map(LogDto::of).toList());
    }


    @GetMapping("/{appId}/logs")
    public ResponseEntity<ApplicationLogsResponseDto> getFilteredLogsByAppId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String appId,
            @ModelAttribute LogFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        final Application app = getApplicationsUseCase.getApplication(appId, userDetails.getUsername());
        PaginatedResult<LogEntry> result = getLogUseCase.getFilteredLogs(
                appId,
                userDetails.getUsername(),
                filterDto.toLogFilter(),
                page,
                size
        );

        List<LogDto> logDtos = result.getContent().stream()
                .map(log -> new LogDto(
                        log.getMessage(),
                        log.getLevel(),
                        log.getLoggerName(),
                        log.getTimestamp().toString()
                ))
                .toList();

        PaginatedResponse<LogDto> response = new PaginatedResponse<>(
                logDtos,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getPage(),
                result.getSize()
        );

        return ResponseEntity.ok(new ApplicationLogsResponseDto(app.getName(), response));
    }

    @GetMapping("/{appId}/metrics")
    public ResponseEntity<ApplicationMetricsResponseDto> getFilteredMetrics(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable String appId,
                                             @ModelAttribute MetricFilterDto filterDto,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        final Application app = getApplicationsUseCase.getApplication(appId, userDetails.getUsername());
        PaginatedResult<MetricEntry> result = getMetricsUseCase.getFilteredMetrics(
                appId,
                userDetails.getUsername(),
                filterDto.toMetricFilter(),
                page,
                size
        );

        List<MetricDto> metricDtos = result.getContent().stream()
                .map(metric -> new MetricDto(
                        metric.getName(),
                        metric.getDurationMillis(),
                        metric.getStatus(),
                        metric.getTimestamp(),
                        metric.getUri(),
                        metric.getHttpMethod(),
                        metric.getHttpStatus()
                ))
                .toList();

        PaginatedResponse<MetricDto> response = new PaginatedResponse<>(
                metricDtos,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getPage(),
                result.getSize()
        );

        return ResponseEntity.ok(new ApplicationMetricsResponseDto(app.getName(), response));
    }

    @GetMapping("/{appId}/usage")
    public ResponseEntity<ApplicationUsageDto> getUsage(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable String appId) {
        return ResponseEntity.ok(ApplicationUsageDto.of(getUsageUseCase.getApplicationUsage(appId, userDetails.getUsername())));
    }
}
