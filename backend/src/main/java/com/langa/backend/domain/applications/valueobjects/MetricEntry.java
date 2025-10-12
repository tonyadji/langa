package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MetricEntry implements Entry {
    private String appKey;
    private String accountKey;

    private String name;
    private long durationMillis;
    private String status;
    private long timestamp;

    private String uri;
    private String httpMethod;
    private int httpStatus;

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
        sizeInBytes += getStringSize(String.valueOf(timestamp));
        sizeInBytes += getStringSize(String.valueOf(httpStatus));
        sizeInBytes += TIMESTAMP_SIZE;
        return sizeInBytes;
    }
}
