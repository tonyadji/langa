package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

public record LogDto(
        @NotBlank(message = "Log message is required")
        @Size(max = 1000, message = "Message cannot exceed 1000 characters")
        String message,
        @NotBlank(message = "Log level is required") String level,
        @NotBlank(message = "Logger name is required") String loggerName,
        @NotBlank(message = "Timestamp is required")
        String timestamp) implements Serializable {

    public LogEntry toLogEntry() {
        try {
            return new LogEntry()
                    .setMessage(message)
                    .setLevel(level)
                    .setLoggerName(loggerName)
                    .setTimestamp(LocalDateTime.parse(timestamp));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LogDto of(LogEntry logEntry) {
        return new LogDto(
                logEntry.getMessage(),
                logEntry.getLevel(),
                logEntry.getLoggerName(),
                logEntry.getTimestamp().toString());
    }
}
