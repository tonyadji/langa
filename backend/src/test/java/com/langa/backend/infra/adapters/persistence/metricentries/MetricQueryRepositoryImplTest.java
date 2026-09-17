package com.langa.backend.infra.adapters.persistence.metricentries;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MetricEntryDocument;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MongoMetricEntryDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricQueryRepositoryImplTest {

    @Mock
    private MongoMetricEntryDao mongoMetricEntryDao;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MetricQueryRepositoryImpl repository;

    @Test
    void findByAppKeyOrderByTimestampDesc_shouldMapDocuments() {
        MetricEntryDocument doc = new MetricEntryDocument();
        doc.setTimestamp(Instant.now());
        when(mongoMetricEntryDao.findByAppKeyOrderByTimestampDesc("app-1")).thenReturn(List.of(doc));

        assertEquals(1, repository.findByAppKeyOrderByTimestampDesc("app-1").size());
    }

    @Test
    void findByAppKeyAndAccountKeyOrderByTimestampDesc_shouldMapDocuments() {
        MetricEntryDocument doc = new MetricEntryDocument();
        doc.setTimestamp(Instant.now());
        when(mongoMetricEntryDao.findByAppKeyAndAccountKeyOrderByTimestampDesc("app-1", "acc-1")).thenReturn(List.of(doc));

        assertEquals(1, repository.findByAppKeyAndAccountKeyOrderByTimestampDesc("app-1", "acc-1").size());
    }

    @Test
    void findFiltered_shouldApplyAllCriteria_andReturnPagedResult() {
        MetricFilter filter = new MetricFilter()
                .setName("http")
                .setStatus("SUCCESS")
                .setUri("/api/test")
                .setHttpMethod("GET")
                .setHttpStatus(200)
                .setDurationGreaterThan(10)
                .setDurationLessThan(1000)
                .setStartDate(LocalDateTime.now().minusDays(1))
                .setEndDate(LocalDateTime.now());

        MetricEntryDocument doc = new MetricEntryDocument();
        doc.setTimestamp(Instant.now());

        when(mongoTemplate.find(any(Query.class), eq(MetricEntryDocument.class))).thenReturn(List.of(doc));
        when(mongoTemplate.count(any(Query.class), eq(MetricEntryDocument.class))).thenReturn(1L);

        PaginatedResult<MetricEntry> result = repository.findFiltered("app-1", "acc-1", filter, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void findFiltered_shouldWork_withEmptyFilter() {
        MetricFilter filter = new MetricFilter();
        when(mongoTemplate.find(any(Query.class), eq(MetricEntryDocument.class))).thenReturn(List.of());
        when(mongoTemplate.count(any(Query.class), eq(MetricEntryDocument.class))).thenReturn(0L);

        PaginatedResult<MetricEntry> result = repository.findFiltered("app-1", "acc-1", filter, 0, 10);

        assertTrue(result.getContent().isEmpty());
    }
}
