package com.langa.backend.domain.applications.valueobjects;

import java.util.List;

public record ApplicationUsageInfo(
        String id,
        String key,
        String name,
        long logSize,
        long metricSize,
        List<ApplicationUsageTrend> trends

) {
}
