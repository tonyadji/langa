package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.infra.rest.common.dto.LogDto;

public record ApplicationLogsResponseDto(String appName,
                                         PaginatedResponse<LogDto> paginatedLogs) {
}
