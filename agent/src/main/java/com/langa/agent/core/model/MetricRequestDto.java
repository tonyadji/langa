package com.langa.agent.core.model;

import java.util.List;

public record MetricRequestDto(String appKey, String accountKey, List<MetricEntry> entries, SendableRequestType type) implements SendableRequestDto {
}
