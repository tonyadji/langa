package com.langa.backend.domain.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.RetentionPolicy;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    private static final IngestionSizeCalculator SIZE_CALCULATOR = content -> content.size() * 10L;

    @Test
    void createNew_shouldRegisterCreatedEvent() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");

        assertEquals("My App", app.getName());
        assertEquals("owner@example.com", app.getOwner());
        assertEquals("ACC-1", app.getAccountKey());
        assertNotNull(app.getKey());
        assertNotNull(app.getSecret());
        assertEquals(1, app.getEvents().size());

        app.clearEvents();
        assertTrue(app.getEvents().isEmpty());
    }

    @Test
    void checkOwnership_shouldPass_whenOwnerMatches() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        assertDoesNotThrow(() -> app.checkOwnership("owner@example.com"));
    }

    @Test
    void checkOwnership_shouldThrow_whenOwnerDoesNotMatch() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        ApplicationException ex = assertThrows(ApplicationException.class, () -> app.checkOwnership("intruder@example.com"));
        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void authorizedToAccess_shouldPass_forOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        assertDoesNotThrow(() -> app.authorizedToAccess("owner@example.com", Set.of()));
    }

    @Test
    void authorizedToAccess_shouldPass_forSharedAccountKey() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);

        assertDoesNotThrow(() -> app.authorizedToAccess("guest@example.com", Set.of("ACC-GUEST")));
    }

    @Test
    void authorizedToAccess_shouldThrow_whenNoAccess() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        assertThrows(ApplicationException.class, () -> app.authorizedToAccess("intruder@example.com", Set.of("ACC-OTHER")));
    }

    @Test
    void shareWith_shouldAddToSharedWithSet() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);

        assertTrue(app.alreadySharedWith("ACC-GUEST"));
        assertEquals(1, app.getSharedWith().size());
    }

    @Test
    void revokeSharing_shouldDeactivateSharing() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);

        app.revokeSharing("ACC-GUEST");

        assertFalse(app.alreadySharedWith("ACC-GUEST"));
    }

    @Test
    void revokeSharing_shouldThrow_whenNoActiveSharingFound() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");

        ApplicationException ex = assertThrows(ApplicationException.class, () -> app.revokeSharing("ACC-GUEST"));
        assertEquals(Errors.APPLICATION_SHARING_NOT_FOUND_TO_REVOKE, ex.getError());
    }

    @Test
    void authorizedToAccess_shouldAllowTheOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        assertDoesNotThrow(() -> app.authorizedToAccess("owner@example.com", Set.of()));
    }

    @Test
    void authorizedToAccess_shouldAllowAUserTheAppIsSharedWith() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);
        assertDoesNotThrow(() -> app.authorizedToAccess("guest@example.com", Set.of("ACC-GUEST")));
    }

    @Test
    void authorizedToAccess_shouldAllowMembersOfATeamTheAppIsSharedWith() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("TEAM-1", SharedWithProfile.TEAM);
        assertDoesNotThrow(() -> app.authorizedToAccess("member@example.com", Set.of("ACC-MEMBER", "TEAM-1")));
    }

    @Test
    void authorizedToAccess_shouldDenyARevokedShare() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);
        app.revokeSharing("ACC-GUEST");

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> app.authorizedToAccess("guest@example.com", Set.of("ACC-GUEST")));
        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void authorizedToAccess_shouldDenyWhenNeitherOwnerNorShared() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        assertThrows(ApplicationException.class,
                () -> app.authorizedToAccess("intruder@example.com", Set.of("ACC-OTHER")));
    }

    @Test
    void createLogEntries_shouldIncreaseUsage() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        LogEntry entry = new LogEntry().setMessage("hello").setLevel("INFO").setTimestamp(Instant.now());

        app.createLogEntries(List.of(entry), SIZE_CALCULATOR);

        assertEquals(10L, app.getUsage().totalLogBytes());
        assertEquals(app.getKey(), entry.getAppKey());
        assertEquals(app.getAccountKey(), entry.getAccountKey());
    }

    @Test
    void createMetricEntries_shouldIncreaseUsage() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        MetricEntry entry = new MetricEntry().setName("http.request").setTimestamp(Instant.now());

        app.createMetricEntries(List.of(entry), SIZE_CALCULATOR);

        assertEquals(10L, app.getUsage().totalMetricBytes());
        assertEquals(app.getKey(), entry.getAppKey());
    }

    @Test
    void updateRetentionPolicy_shouldInitializeDefault_whenNull() {
        Application app = Application.populate(app().getAppId(), "My App",
                new com.langa.backend.domain.applications.valueobjects.ApplicationOwner("ACC-1", "owner@example.com"),
                java.util.Collections.emptySet(),
                com.langa.backend.domain.applications.valueobjects.ApplicationUsage.empty());

        app.updateRetentionPolicy(new RetentionPolicy(10, ChronoUnit.DAYS, null));

        assertEquals(10, app.getRetentionPolicy().duration());
    }

    private Application app() {
        return Application.createNew("My App", "ACC-1", "owner@example.com");
    }
}
