package com.langa.backend.infra.persistence.repositories.metricentries.mongo;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "c_metrics")
@Data
public class MetricEntryDocument {
    @Id
    private String id;
    private String appKey;
    private String accountKey;

    private String name;
    private long durationMillis;
    private String status;
    private long timestamp;
    private String uri;
    private String httpMethod;
    private int httpStatus;

    public MetricEntry toMetricEntry() {
        return new MetricEntry()
                .setName(name)
                .setDurationMillis(durationMillis)
                .setStatus(status)
                .setTimestamp(timestamp)
                .setUri(uri)
                .setHttpMethod(httpMethod)
                .setHttpStatus(httpStatus);
    }

    public static MetricEntryDocument of(MetricEntry metricEntry) {
        MetricEntryDocument metricEntryDocument = new MetricEntryDocument();
        metricEntryDocument.setAppKey(metricEntry.getAppKey());
        metricEntryDocument.setAccountKey(metricEntry.getAccountKey());
        metricEntryDocument.setName(metricEntry.getName());
        metricEntryDocument.setDurationMillis(metricEntry.getDurationMillis());
        metricEntryDocument.setStatus(metricEntry.getStatus());
        metricEntryDocument.setTimestamp(metricEntry.getTimestamp());
        metricEntryDocument.setUri(metricEntry.getUri());
        metricEntryDocument.setHttpMethod(metricEntry.getHttpMethod());
        metricEntryDocument.setHttpStatus(metricEntry.getHttpStatus());
        return metricEntryDocument;
    }
}
