package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.usecases.CreateApplicationUseCase;
import com.langa.backend.domain.applications.usecases.GetApplicationsUseCase;
import com.langa.backend.domain.applications.usecases.GetLogUseCase;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.rest.applications.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final GetApplicationsUseCase getApplicationsUseCase;
    private final GetLogUseCase getLogUseCase;
    private final CreateApplicationUseCase createApplicationUseCase;

    public ApplicationController(GetApplicationsUseCase getApplicationsUseCase, GetLogUseCase getLogUseCase, CreateApplicationUseCase createApplicationUseCase) {
        this.getApplicationsUseCase = getApplicationsUseCase;
        this.getLogUseCase = getLogUseCase;
        this.createApplicationUseCase = createApplicationUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationDto> createApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody CreateApplicationRequestDto applicationRequestDto) {

        return ResponseEntity.ok(ApplicationDto.of(createApplicationUseCase.create(applicationRequestDto.name(), userDetails.getUsername())));
    }

    @GetMapping()
    public ResponseEntity<List<ApplicationDto>> getAllApplications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(getApplicationsUseCase.getApplications(userDetails.getUsername())
                .stream().map(ApplicationDto::of).collect(Collectors.toList()));
    }

    public ResponseEntity<List<LogDto>> getLogsByAppKey(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String appId) {
        return ResponseEntity.ok(getLogUseCase.getLogs(appId, userDetails.getUsername())
                .stream().map(LogDto::of).collect(Collectors.toList()));
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
                .collect(Collectors.toList());

        PaginatedResponse<LogDto> response = new PaginatedResponse<>(
                logDtos,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getPage(),
                result.getSize()
        );

        return ResponseEntity.ok(new ApplicationLogsResponseDto(app.getName(), response));
    }

}
