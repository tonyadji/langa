package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FirstConnectionMailTemplateTest {

    private final FirstConnectionMailTemplate template = new FirstConnectionMailTemplate("http://localhost:3000");

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com");
        ActiveUserRegisteredEvent other = new ActiveUserRegisteredEvent("user-1", "user@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com");

        template.processEvent(event);

        assertEquals("First Connection", template.getSubject());
        assertTrue(template.getMessage().contains("http://localhost:3000/login"));
        assertFalse(template.getMessage().contains("first-connection"));
        assertEquals(List.of("user@example.com"), template.getRecipients());
    }
}
