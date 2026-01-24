package com.langa.backend.infra.adapters.persistence.metricentries.mongo;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "c_metrics")
@Data
public class MetricEntryDocument {
    @Id
    private String id;
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
    @Indexed(name = "ttl_dynamic_index", expireAfter = "0")
    private Instant expiresAt;

    public MetricEntry toMetricEntry() {
        return new MetricEntry()
                .setName(name)
                .setSignature(signature)
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
        metricEntryDocument.setSignature(metricEntry.getSignature());
        metricEntryDocument.setDurationMillis(metricEntry.getDurationMillis());
        metricEntryDocument.setStatus(metricEntry.getStatus());
        metricEntryDocument.setTimestamp(metricEntry.getTimestamp());
        metricEntryDocument.setUri(metricEntry.getUri());
        metricEntryDocument.setHttpMethod(metricEntry.getHttpMethod());
        metricEntryDocument.setHttpStatus(metricEntry.getHttpStatus());
        metricEntryDocument.setExpiresAt(Instant.now().plus(metricEntry.getRetentionDuration(), metricEntry.getRetentionUnit()));
        return metricEntryDocument;
    }
}
