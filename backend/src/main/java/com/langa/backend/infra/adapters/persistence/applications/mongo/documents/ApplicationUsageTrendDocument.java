package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsage;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageTrend;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "c_application_usage_trends")
public class ApplicationUsageTrendDocument {
    @Id
    private String id;
    private String name;
    private String appKey;
    private String owner;
    private long usage;
    private IngestionType type;
    private Instant createdDate;

    public static ApplicationUsageTrendDocument ofLog(Application application) {
        final ApplicationUsageTrendDocument appUsageTrend = of(application);
        appUsageTrend.setType(IngestionType.LOG);
        appUsageTrend.setUsage(application.getPendingLogBytes());
        return appUsageTrend;
    }

    public static ApplicationUsageTrendDocument ofMetric(Application application) {
        final ApplicationUsageTrendDocument appUsageTrend = of(application);
        appUsageTrend.setType(IngestionType.METRIC);
        appUsageTrend.setUsage(application.getPendingMetricBytes());
        return appUsageTrend;
    }

    private static ApplicationUsageTrendDocument of (Application application){
        final ApplicationUsageTrendDocument appUsageTrend = new ApplicationUsageTrendDocument();
        appUsageTrend.setName(application.getName());
        appUsageTrend.setAppKey(application.getKey());
        appUsageTrend.setOwner(application.getOwner());
        appUsageTrend.createdDate = Instant.now();
        return appUsageTrend;
    }

    public ApplicationUsageTrend toApplicationTrend() {
        return new ApplicationUsageTrend(name, appKey, usage, type, createdDate);
    }
}
