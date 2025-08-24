package com.langa.backend.infra.rest.applications.dto;

public record ApplicationLogsResponseDto(String appName,
                                        PaginatedResponse<LogDto> paginatedLogs) {
}
