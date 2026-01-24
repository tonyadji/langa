package com.capricedumardi.agent.core.model;

import java.util.List;

public record LogRequestDto(String appKey, String accountKey, List<LogEntry> entries, SendableRequestType type) implements SendableRequestDto {
}
