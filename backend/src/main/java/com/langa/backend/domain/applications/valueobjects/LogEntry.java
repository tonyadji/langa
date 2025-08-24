package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LogEntry {
    private String appKey;
    private String accountKey;
    private String message;
    private String level;
    private String loggerName;
    private LocalDateTime timestamp;
}
