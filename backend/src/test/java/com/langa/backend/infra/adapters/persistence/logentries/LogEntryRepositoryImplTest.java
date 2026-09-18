package com.langa.backend.infra.adapters.persistence.logentries;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.LogEntryDocument;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.MongoLogEntryDao;
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
class LogEntryRepositoryImplTest {

    @Mock
    private MongoLogEntryDao mongoLogEntryDao;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LogEntryRepositoryImpl repository;

    @Test
    void findByAppKeyOrderByTimestampDesc_shouldMapDocuments() {
        LogEntryDocument doc = new LogEntryDocument();
        doc.setTimestamp(Instant.now());
        when(mongoLogEntryDao.findByAppKeyOrderByTimestampDesc("app-1")).thenReturn(List.of(doc));

        List<LogEntry> result = repository.findByAppKeyOrderByTimestampDesc("app-1");

        assertEquals(1, result.size());
    }

    @Test
    void findByAppKeyAndAccountKeyOrderByTimestampDesc_shouldMapDocuments() {
        LogEntryDocument doc = new LogEntryDocument();
        doc.setTimestamp(Instant.now());
        when(mongoLogEntryDao.findByAppKeyAndAccountKeyOrderByTimestampDesc("app-1", "acc-1")).thenReturn(List.of(doc));

        assertEquals(1, repository.findByAppKeyAndAccountKeyOrderByTimestampDesc("app-1", "acc-1").size());
    }

    @Test
    void findFiltered_shouldApplyAllCriteria_andReturnPagedResult() {
        LogFilter filter = new LogFilter("INFO,WARN", "keyword", LocalDateTime.now().minusDays(1), LocalDateTime.now());
        LogEntryDocument doc = new LogEntryDocument();
        doc.setTimestamp(Instant.now());

        when(mongoTemplate.find(any(Query.class), eq(LogEntryDocument.class))).thenReturn(List.of(doc));
        when(mongoTemplate.count(any(Query.class), eq(LogEntryDocument.class))).thenReturn(1L);

        PaginatedResult<LogEntry> result = repository.findFiltered("app-1", filter, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void findFiltered_shouldWork_withEmptyFilter() {
        LogFilter filter = new LogFilter(null, null, null, null);
        when(mongoTemplate.find(any(Query.class), eq(LogEntryDocument.class))).thenReturn(List.of());
        when(mongoTemplate.count(any(Query.class), eq(LogEntryDocument.class))).thenReturn(0L);

        PaginatedResult<LogEntry> result = repository.findFiltered("app-1", filter, 0, 10);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalPages());
    }
}
