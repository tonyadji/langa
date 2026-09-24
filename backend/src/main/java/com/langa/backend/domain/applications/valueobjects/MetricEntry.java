package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@Accessors(chain = true)
public class MetricEntry implements Entry {
    private String appKey;
    private String accountKey;

    private String name;
    private String signature;
    private Integer durationMillis;
    private String status;
    private Instant timestamp;

    private String uri;
    private String httpMethod;
    private int httpStatus;
    private RetentionPolicy retention;

    @Override
    public long getSizeInBytes() {
        long sizeInBytes = BASE_DOCUMENT_OVERHEAD;
        sizeInBytes += getStringSize(appKey);
        sizeInBytes += getStringSize(accountKey);
        sizeInBytes += getStringSize(name);
        sizeInBytes += getStringSize(uri);
        sizeInBytes += getStringSize(httpMethod);
        sizeInBytes += getStringSize(status);
        sizeInBytes += getStringSize(String.valueOf(durationMillis));
        sizeInBytes += getStringSize(timestamp.toString());
        sizeInBytes += getStringSize(String.valueOf(httpStatus));
        sizeInBytes += TIMESTAMP_SIZE;
        return sizeInBytes;
    }

    public long getRetentionDuration() {
        return retention.duration();
    }

    public ChronoUnit getRetentionUnit() {
        return retention.unit();
    }
}
