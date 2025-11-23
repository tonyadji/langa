package com.langa.backend.infra.adapters.in.rest.ingest.dto;

import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.infra.adapters.in.rest.common.dto.MetricDto;

import java.util.List;

public record MetricIngestionRequestDto(
        String appKey,
        String accountKey,
        List<MetricDto> entries, IngestionType type) implements IngestionRequestDto {

        public List<MetricEntry> getEntries() {
            return entries.stream()
                    .map(MetricDto::toMetricEntry)
                    .toList();
        }
}
