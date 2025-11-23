package com.langa.backend.infra.adapters.in.rest.common.dto;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import jakarta.validation.constraints.NotNull;

public record MetricDto(
        @NotNull String name,
        Integer durationMillis,
        @NotNull String status,
        String timestamp,

        String uri,
        String httpMethod,
        Integer httpStatus
) {

    public MetricEntry toMetricEntry () {
        return new MetricEntry()
                .setName(name)
                .setDurationMillis(durationMillis)
                .setStatus(status)
                .setTimestamp(timestamp)
                .setUri(uri)
                .setHttpMethod(httpMethod)
                .setHttpStatus(httpStatus);
    }
}
