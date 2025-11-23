package com.langa.backend.domain.applications.valueobjects;

public record ApplicationUsageInfo(
        String id,
        String key,
        String name,
        long logSize,
        long metricSize

) {
}
