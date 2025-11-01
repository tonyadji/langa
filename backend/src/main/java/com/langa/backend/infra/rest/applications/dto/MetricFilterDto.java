package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record MetricFilterDto(
        String name,
        String status,
        String uri,
        String httpMethod,
        Integer httpStatus,
        Integer durationLessThan,
        Integer durationGreaterThan,
        String keyword,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate
) {
    public MetricFilter toMetricFilter() {
        return new MetricFilter()
                .setName(name)
                .setStatus(status)
                .setUri(uri)
                .setHttpMethod(httpMethod)
                .setHttpStatus(httpStatus)
                .setDurationLessThan(durationLessThan)
                .setDurationGreaterThan(durationGreaterThan)
                .setKeyword(keyword)
                .setStartDate(startDate)
                .setEndDate(endDate);
    }
}
