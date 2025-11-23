package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.infra.rest.common.dto.MetricDto;

public record ApplicationMetricsResponseDto(String appName,
                                            PaginatedResponse<MetricDto> paginatedMetrics) {
}
