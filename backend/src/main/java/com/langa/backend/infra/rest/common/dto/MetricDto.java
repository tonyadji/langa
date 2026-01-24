package com.langa.backend.infra.rest.common.dto;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MetricDto(
        @NotNull String name,
        String signature,
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
                .setSignature(signature)
                .setDurationMillis(durationMillis)
                .setStatus(status)
                .setTimestamp(Instant.parse(timestamp))
                .setUri(uri)
                .setHttpMethod(httpMethod)
                .setHttpStatus(httpStatus);
    }
}
