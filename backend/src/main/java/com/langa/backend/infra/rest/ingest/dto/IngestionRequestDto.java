package com.langa.backend.infra.rest.ingest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LogIngestionRequestDto.class, name = "LOG"),
        @JsonSubTypes.Type(value = MetricIngestionRequestDto.class, name = "METRIC")
})
public sealed interface IngestionRequestDto permits LogIngestionRequestDto, MetricIngestionRequestDto {
}
