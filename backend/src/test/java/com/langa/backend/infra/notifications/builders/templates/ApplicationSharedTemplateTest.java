package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationSharedTemplateTest {

    private final ApplicationSharedTemplate template = new ApplicationSharedTemplate("http://localhost:3000");

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        ApplicationSharedEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");
        ActiveUserRegisteredEvent other = new ActiveUserRegisteredEvent("user-1", "user@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        ApplicationSharedEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");

        template.processEvent(event);

        assertEquals("Application sharing", template.getSubject());
        assertTrue(template.getMessage().contains("My App"));
        assertEquals(List.of("guest@example.com"), template.getRecipients());
    }
}
