package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;

public record ApplicationUsageDto(
        String id,
        String key,
        String name,
        long logUsage,
        long metricUsage
) {

    public static ApplicationUsageDto of(ApplicationUsageInfo usage) {
        if (usage == null) {
            return ApplicationUsageDto.empty();
        }
        return new ApplicationUsageDto(usage.id(), usage.key(), usage.name(), usage.logSize(), usage.metricSize());
    }

    private static ApplicationUsageDto empty() {
        return new ApplicationUsageDto(null, null, null, 0L, 0L);
    }

    public static ApplicationUsageDto of(Application app) {
        if (app.getUsage() != null) {
            return  new ApplicationUsageDto(app.getId(), app.getKey(), app.getName(), app.getUsage().totalLogBytes(), app.getUsage().totalMetricBytes());
        }
        return  new ApplicationUsageDto(app.getId(), app.getKey(), app.getName(), 0L, 0L);
    }
}
