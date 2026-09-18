package com.langa.backend.infra.adapters.persistence.applications;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoApplicationDao;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoApplicationUsageTrendDao;
import com.langa.backend.infra.adapters.persistence.applications.mongo.daos.MongoDeletedApplicationDao;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.ApplicationDocument;
import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.UsageSumDto;
import com.langa.backend.infra.adapters.persistence.logentries.mongo.MongoLogEntryDao;
import com.langa.backend.infra.adapters.persistence.metricentries.mongo.MongoMetricEntryDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationRepositoryImplTest {

    @Mock
    private MongoApplicationDao mongoApplicationDao;
    @Mock
    private MongoLogEntryDao mongoLogEntryDao;
    @Mock
    private MongoMetricEntryDao mongoMetricEntryDao;
    @Mock
    private MongoApplicationUsageTrendDao mongoApplicationUsageTrendDao;
    @Mock
    private MongoDeletedApplicationDao mongoDeletedApplicationDao;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ApplicationRepositoryImpl repository;

    private Application app() {
        return Application.createNew("My App", "ACC-1", "owner@example.com");
    }

    @Test
    void save_shouldPersistLogEntries_whenPresent() {
        Application app = app();
        app.createLogEntries(List.of(new LogEntry().setMessage("hi").setTimestamp(Instant.now())), l -> 10L);
        when(mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(anyString(), any())).thenReturn(Optional.empty());
        when(mongoApplicationDao.save(any(ApplicationDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        Application saved = repository.save(app);

        assertNotNull(saved);
        verify(mongoLogEntryDao).saveAll(anyList());
        verify(mongoApplicationUsageTrendDao).save(any());
    }

    @Test
    void save_shouldPersistMetricEntries_whenPresent() {
        Application app = app();
        app.createMetricEntries(List.of(new MetricEntry().setName("m").setTimestamp(Instant.now())), l -> 10L);
        when(mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(anyString(), any())).thenReturn(Optional.empty());
        when(mongoApplicationDao.save(any(ApplicationDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        Application saved = repository.save(app);

        assertNotNull(saved);
        verify(mongoMetricEntryDao).saveAll(anyList());
    }

    @Test
    void save_shouldComputeUsageFromTrendSums() {
        Application app = app();
        when(mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(app.getKey(), IngestionType.LOG))
                .thenReturn(Optional.of(new UsageSumDto(100L)));
        when(mongoApplicationUsageTrendDao.sumUsageByAppKeyAndType(app.getKey(), IngestionType.METRIC))
                .thenReturn(Optional.of(new UsageSumDto(50L)));
        when(mongoApplicationDao.save(any(ApplicationDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        Application saved = repository.save(app);

        assertEquals(100L, saved.getUsage().totalLogBytes());
        assertEquals(50L, saved.getUsage().totalMetricBytes());
        verifyNoInteractions(mongoLogEntryDao, mongoMetricEntryDao);
    }

    @Test
    void findByKey_shouldReturnApplication_whenFound() {
        Application app = app();
        when(mongoApplicationDao.findByKey("key-1")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        Application result = repository.findByKey("key-1");

        assertEquals(app.getName(), result.getName());
    }

    @Test
    void findByKey_shouldThrow_whenNotFound() {
        when(mongoApplicationDao.findByKey("key-1")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> repository.findByKey("key-1"));
    }

    @Test
    void count_shouldDelegateToDao() {
        when(mongoApplicationDao.count()).thenReturn(5L);
        assertEquals(5L, repository.count());
    }

    @Test
    void findAll_shouldMapAllDocuments() {
        Application app = app();
        when(mongoApplicationDao.findAll()).thenReturn(List.of(ApplicationDocument.of(app)));

        List<Application> results = repository.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(mongoApplicationDao.findById("id-1")).thenReturn(Optional.empty());
        assertTrue(repository.findById("id-1").isEmpty());
    }

    @Test
    void findById_shouldReturnApplication_whenFound() {
        Application app = app();
        when(mongoApplicationDao.findById("id-1")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        assertTrue(repository.findById("id-1").isPresent());
    }

    @Test
    void findByKeyAndAccountKey_shouldReturnApplication() {
        Application app = app();
        when(mongoApplicationDao.findByKeyAndAccountKey("key-1", "ACC-1")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        assertTrue(repository.findByKeyAndAccountKey("key-1", "ACC-1").isPresent());
    }

    @Test
    void findByAccountKey_shouldMapDocuments() {
        Application app = app();
        when(mongoApplicationDao.findByAccountKey("ACC-1")).thenReturn(List.of(ApplicationDocument.of(app)));

        assertEquals(1, repository.findByAccountKey("ACC-1").size());
    }

    @Test
    void findByOwner_shouldMapDocuments() {
        Application app = app();
        when(mongoApplicationDao.findByOwner("owner@example.com")).thenReturn(List.of(ApplicationDocument.of(app)));

        assertEquals(1, repository.findByOwner("owner@example.com").size());
    }

    @Test
    void findByOwnerAndName_shouldReturnApplication() {
        Application app = app();
        when(mongoApplicationDao.findByOwnerAndName("owner@example.com", "My App")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        assertTrue(repository.findByOwnerAndName("owner@example.com", "My App").isPresent());
    }

    @Test
    void findByIdAndOwner_shouldReturnApplication() {
        Application app = app();
        when(mongoApplicationDao.findByIdAndOwner("id-1", "owner@example.com")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        assertTrue(repository.findByIdAndOwner("id-1", "owner@example.com").isPresent());
    }

    @Test
    void securedFindByIdAndOwner_shouldReturnSecuredApplication() {
        Application app = app();
        when(mongoApplicationDao.findByIdAndOwner("id-1", "owner@example.com")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        Optional<Application> result = repository.securedFindByIdAndOwner("id-1", "owner@example.com");

        assertTrue(result.isPresent());
        assertEquals(app.getSecret(), result.get().getSecret());
    }

    @Test
    void findApplicationUsageTrends_shouldMapDocuments() {
        when(mongoApplicationUsageTrendDao.findByAppKeyOrderByCreatedDateDesc("key-1")).thenReturn(List.of());

        assertTrue(repository.findApplicationUsageTrends("key-1").isEmpty());
    }

    @Test
    void deleteById_shouldArchiveAndDelete_whenFound() {
        Application app = app();
        when(mongoApplicationDao.findById("id-1")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        repository.deleteById("id-1");

        verify(mongoDeletedApplicationDao).save(any());
        verify(mongoApplicationDao).deleteById("id-1");
    }

    @Test
    void deleteById_shouldDoNothing_whenNotFound() {
        when(mongoApplicationDao.findById("id-1")).thenReturn(Optional.empty());

        repository.deleteById("id-1");

        verify(mongoDeletedApplicationDao, never()).save(any());
        verify(mongoApplicationDao, never()).deleteById(anyString());
    }

    @Test
    void findBySharedWithUser_shouldMapDocuments() {
        Application app = app();
        when(mongoApplicationDao.findBySharedWith_KeyAndSharedWith_ExpirationDateIsNullAndSharedWith_RevokedDateIsNull("ACC-GUEST"))
                .thenReturn(List.of(ApplicationDocument.of(app)));

        assertEquals(1, repository.findBySharedWithUser("ACC-GUEST").size());
    }

    @Test
    void findBySharedWithTeams_shouldMapDocuments() {
        Application app = app();
        when(mongoApplicationDao.findBySharedWith_KeyInAndSharedWith_ExpirationDateIsNullAndSharedWith_RevokedDateIsNull(Set.of("team-1")))
                .thenReturn(List.of(ApplicationDocument.of(app)));

        assertEquals(1, repository.findBySharedWithTeams(Set.of("team-1")).size());
    }

    @Test
    void findSecuredAppByKeyAndAccountKey_shouldReturnSecuredApplication() {
        Application app = app();
        when(mongoApplicationDao.findByKeyAndAccountKey("key-1", "ACC-1")).thenReturn(Optional.of(ApplicationDocument.of(app)));

        Optional<Application> result = repository.findSecuredAppByKeyAndAccountKey("key-1", "ACC-1");

        assertTrue(result.isPresent());
        assertEquals(app.getSecret(), result.get().getSecret());
    }
}
