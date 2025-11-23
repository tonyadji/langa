package com.langa.backend.infra.adapters.in.rest.applications.dto;

import com.langa.backend.infra.adapters.in.rest.common.dto.MetricDto;

public record ApplicationMetricsResponseDto(String appName,
                                            PaginatedResponse<MetricDto> paginatedMetrics) {
}
