package com.langa.backend.infra.adapters.persistence.logentries.mongo;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "c_logs")
public class LogEntryDocument {
    @Id
    private String id;
    private String appKey;
    private String accountKey;
    private String message;
    private String level;
    private String loggerName;
    private Instant timestamp;
    private String threadName;
    private String stackTrace;
    private Map<String, String> mdc;

    public LogEntry toLogEntry() {
        return new LogEntry()
                .setAppKey(appKey)
                .setAccountKey(accountKey)
                .setMessage(message)
                .setLevel(level)
                .setLoggerName(loggerName)
                .setTimestamp(timestamp)
                .setThreadName(threadName)
                .setStackTrace(stackTrace)
                .setMdc(mdc);
    }

    public static LogEntryDocument of(LogEntry logEntry) {
        LogEntryDocument logEntryDocument = new LogEntryDocument();
        logEntryDocument.setAppKey(logEntry.getAppKey());
        logEntryDocument.setAccountKey(logEntry.getAccountKey());
        logEntryDocument.setMessage(logEntry.getMessage());
        logEntryDocument.setLevel(logEntry.getLevel());
        logEntryDocument.setLoggerName(logEntry.getLoggerName());
        logEntryDocument.setTimestamp(logEntry.getTimestamp());
        logEntryDocument.setThreadName(logEntry.getThreadName());
        logEntryDocument.setStackTrace(logEntry.getStackTrace());
        logEntryDocument.setMdc(logEntry.getMdc());
        return logEntryDocument;
    }
}
