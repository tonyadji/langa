package com.langa.backend.infra.adapters.in.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;

public record ApplicationUsageDto(
        String id,
        String key,
        String name,
        long logUsage,
        long metricUsage
) {

    public static ApplicationUsageDto of(ApplicationUsageInfo usage) {
        return new ApplicationUsageDto(usage.id(), usage.key(), usage.name(), usage.logSize(), usage.metricSize());
    }
}
