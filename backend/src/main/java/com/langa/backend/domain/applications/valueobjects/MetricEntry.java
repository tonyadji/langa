package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MetricEntry {
    private String appKey;
    private String accountKey;

    private String name;
    private long durationMillis;
    private String status;
    private long timestamp;

    private String uri;
    private String httpMethod;
    private int httpStatus;
}
