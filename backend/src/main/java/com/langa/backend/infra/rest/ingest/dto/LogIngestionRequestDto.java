package com.langa.backend.infra.rest.ingest.dto;

import com.langa.backend.infra.rest.common.dto.LogDto;

import java.util.List;

public record LogIngestionRequestDto(
        String appKey,
        String accountKey,
        List<LogDto> entries,
        IngestionType type) implements IngestionRequestDto{


}
