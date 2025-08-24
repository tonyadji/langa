package com.langa.backend.infra.persistence.repositories.logentries.mongo;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "logs")
public class LogEntryDocument {
    private String id;
    private String appKey;
    private String accountKey;
    private String message;
    private String level;
    private String loggerName;
    private LocalDateTime timestamp;

    public LogEntry toLogEntry() {
        return new LogEntry()
                .setAppKey(appKey)
                .setAccountKey(accountKey)
                .setMessage(message)
                .setLevel(level)
                .setLoggerName(loggerName)
                .setTimestamp(timestamp);
    }

    public static LogEntryDocument of(LogEntry logEntry) {
        LogEntryDocument logEntryDocument = new LogEntryDocument();
        logEntryDocument.setAppKey(logEntry.getAppKey());
        logEntryDocument.setAccountKey(logEntry.getAccountKey());
        logEntryDocument.setMessage(logEntry.getMessage());
        logEntryDocument.setLevel(logEntry.getLevel());
        logEntryDocument.setLoggerName(logEntry.getLoggerName());
        logEntryDocument.setTimestamp(logEntry.getTimestamp());
        return logEntryDocument;
    }
}
