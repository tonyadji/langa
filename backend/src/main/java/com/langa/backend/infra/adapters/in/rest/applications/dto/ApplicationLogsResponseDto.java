package com.langa.backend.infra.adapters.in.rest.applications.dto;

import com.langa.backend.infra.adapters.in.rest.common.dto.LogDto;

public record ApplicationLogsResponseDto(String appName,
                                         PaginatedResponse<LogDto> paginatedLogs) {
}
