package com.langa.backend.infra.demo;

import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.infra.security.config.AuthProviderProperties;
import com.langa.backend.infra.security.localauth.LocalIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private UserService userService;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private IngestionSizeCalculator ingestionSizeCalculator;

    private final DemoDataProperties properties = new DemoDataProperties();
    private final AuthProviderProperties authProperties = new AuthProviderProperties();
    private DemoDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DemoDataSeeder(properties, authProperties, userService, applicationRepository,
                ingestionSizeCalculator, "http://localhost:8080/api/ingestion");
    }

    @Test
    void run_shouldDoNothing_whenTheDemoApplicationExists() {
        when(applicationRepository.findById(properties.getAppId())).thenReturn(Optional.of(mock(Application.class)));

        seeder.run(null);

        verify(applicationRepository, never()).save(any());
        verifyNoInteractions(userService);
    }

    @Test
    void run_shouldCreateTheLocalDemoUserAndItsApplicationWithHistory() {
        authProperties.setProvider(LocalIdentity.PROVIDER);
        ExternalIdentity identity = new ExternalIdentity(
                LocalIdentity.PROVIDER, LocalIdentity.subjectFor("demo@langa.local"), "demo@langa.local");
        User owner = User.createFromExternalIdentity(identity);
        when(applicationRepository.findById(properties.getAppId())).thenReturn(Optional.empty());
        when(userService.provisionExternalUser(identity)).thenReturn(owner);
        when(ingestionSizeCalculator.calculateSizeInBytes(anyList())).thenReturn(100L);

        seeder.run(null);

        ArgumentCaptor<Application> saved = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(saved.capture());
        Application application = saved.getValue();
        assertEquals(properties.getAppId(), application.getId());
        assertEquals("APP-DemoShop", application.getKey());
        assertEquals("Demo Shop", application.getName());
        assertEquals(owner.getAccountKey(), application.getAccountKey());
        assertEquals("demo@langa.local", application.getOwner());
        assertEquals(properties.getAppSecret(), application.getSecret());
        assertEquals(KeyGenerator.generateIngestionUri(owner.getAccountKey(), "APP-DemoShop"), application.getIngestionUri());
        assertEquals(24 * 30, application.getNewLogEntries().size());
        assertEquals(24 * 30, application.getNewMetricsEntries().size());
        assertTrue(application.getNewLogEntries().stream().allMatch(log -> "APP-DemoShop".equals(log.getAppKey())));
        assertEquals(100L, application.getUsage().totalLogBytes());
    }

    @Test
    void run_shouldCreateAnUnlinkedDemoUser_withAnotherProvider() {
        authProperties.setProvider("entra");
        User owner = User.createFromExternalIdentity(new ExternalIdentity("entra", "oid", "demo@langa.local"));
        when(applicationRepository.findById(properties.getAppId())).thenReturn(Optional.empty());
        when(userService.findOrCreateUserByEmail("demo@langa.local")).thenReturn(owner);

        seeder.run(null);

        verify(userService, never()).provisionExternalUser(any());
        verify(applicationRepository).save(any());
    }

    @Test
    void history_shouldCoverThePeriodWithVariedEntries() {
        Instant to = Instant.now();
        Instant from = to.minus(Duration.ofHours(6));

        List<LogEntry> logs = DemoDataSeeder.logs(from, to, new Random(1));
        List<MetricEntry> metrics = DemoDataSeeder.metrics(from, to, new Random(1));

        assertEquals(6 * 30, logs.size());
        assertTrue(logs.stream().allMatch(log -> !log.getTimestamp().isBefore(from) && log.getTimestamp().isBefore(to)));
        assertEquals(3, logs.stream().map(LogEntry::getLevel).distinct().count());
        assertTrue(logs.stream().filter(log -> "ERROR".equals(log.getLevel())).allMatch(log -> log.getStackTrace() != null));
        assertEquals(6 * 30, metrics.size());
        assertTrue(metrics.stream().allMatch(metric -> metric.getDurationMillis() >= 20));
        assertTrue(metrics.stream().anyMatch(metric -> "SUCCESS".equals(metric.getStatus())));
    }
}
