package com.langa.backend.infra.rest.ingest.dto;

import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.infra.rest.common.dto.LogDto;

import java.util.List;

public record LogIngestionRequestDto(
        String appKey,
        String accountKey,
        List<LogDto> entries,
        IngestionType type) implements IngestionRequestDto{


    public List<LogEntry> getEntries() {
        return entries.stream()
                .map(LogDto::toLogEntry)
                .toList();
    }
}
