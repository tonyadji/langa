package com.langa.agent.model;

import java.util.List;

public record LogRequestDto(String appKey, String accountKey, List<LogEntry> logs) {
}
