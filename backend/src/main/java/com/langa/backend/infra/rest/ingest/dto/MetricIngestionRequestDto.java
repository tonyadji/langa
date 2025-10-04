package com.langa.backend.infra.rest.ingest.dto;

import com.langa.backend.infra.rest.common.dto.MetricDto;

import java.util.List;

public record MetricIngestionRequestDto(
        String appKey,
        String accountKey,
        List<MetricDto> entries, IngestionType type) implements IngestionRequestDto {


}
