package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LogEntry implements Entry {
    private String appKey;
    private String accountKey;
    private String message;
    private String level;
    private String loggerName;
    private LocalDateTime timestamp;

    @Override
    public long getSizeInBytes() {
        long sizeInBytes = BASE_DOCUMENT_OVERHEAD;
        sizeInBytes += getStringSize(appKey);
        sizeInBytes += getStringSize(accountKey);
        sizeInBytes += getStringSize(message);
        sizeInBytes += getStringSize(level);
        sizeInBytes += getStringSize(loggerName);
        sizeInBytes += getStringSize(timestamp.toString());
        sizeInBytes += TIMESTAMP_SIZE;
        return sizeInBytes;
    }
}
