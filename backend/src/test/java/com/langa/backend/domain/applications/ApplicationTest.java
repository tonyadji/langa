package com.langa.backend.domain.applications;

import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.infra.adapters.in.rest.common.dto.LogDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    @Test
    void createNew_shouldInitializeFields() {
        Application app = Application.createNew("MyApp", "accountKey123", "owner@example.com");

        assertEquals("MyApp", app.getName());
        assertEquals("accountKey123", app.getAccountKey());
        assertEquals("owner@example.com", app.getOwner());
        assertNotNull(app.getKey());
    }

    @Test
    void createLogEntries_shouldMapDtosToLogEntries() {
        Application app = Application.createNew("MyApp", "accountKey123", "owner@example.com");

        LogDto logDto = new LogDto("message", "INFO", "loggerName", "2025-08-23T12:00:00", null, null, null);
        List<LogEntry> logEntries = app.createLogEntries(List.of(logDto.toLogEntry()));

        assertEquals(1, logEntries.size());
        LogEntry entry = logEntries.get(0);
        assertEquals("message", entry.getMessage());
        assertEquals("INFO", entry.getLevel());
        assertEquals("loggerName", entry.getLoggerName());
        assertEquals("accountKey123", entry.getAccountKey());
        assertEquals(app.getKey(), entry.getAppKey());
    }

    @Test
    void checkOwnership_withCorrectOwner_shouldNotThrow() {
        Application app = Application.createNew("MyApp", "accountKey123", "owner@example.com");
        assertDoesNotThrow(() -> app.checkOwnership("owner@example.com"));
    }

    @Test
    void checkOwnership_withWrongOwner_shouldThrow() {
        Application app = Application.createNew("MyApp", "accountKey123", "owner@example.com");
        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> app.checkOwnership("intruder@example.com"));
        assertEquals("Access denied", ex.getError().getMessage());
    }
}