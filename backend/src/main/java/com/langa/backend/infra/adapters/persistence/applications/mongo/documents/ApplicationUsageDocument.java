package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import com.langa.backend.domain.applications.ApplicationUsage;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "c_application_usage")
@Data
public class ApplicationUsageDocument {

    @Id
    private String id;
    private String appKey;
    private long totalLogBytes;
    private long totalMetricBytes;

    public ApplicationUsage toApplicationUsage() {
        return new ApplicationUsage(id, appKey, totalLogBytes, totalMetricBytes);
    }

    public static ApplicationUsageDocument of(ApplicationUsage applicationUsage) {
        ApplicationUsageDocument applicationUsageDocument = new ApplicationUsageDocument();
        applicationUsageDocument.setId(applicationUsage.id());
        applicationUsageDocument.setAppKey(applicationUsage.appKey());
        applicationUsageDocument.setTotalLogBytes(applicationUsage.totalLogBytes());
        applicationUsageDocument.setTotalMetricBytes(applicationUsage.totalMetricBytes());
        return applicationUsageDocument;
    }
}
