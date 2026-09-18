package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FirstConnectionMailTemplateTest {

    private final FirstConnectionMailTemplate template = new FirstConnectionMailTemplate("http://localhost:3000");

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com", "token-1");
        AccountSetupCompleteMailEvent other = new AccountSetupCompleteMailEvent("user-1", "user@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com", "token-1");

        template.processEvent(event);

        assertEquals("First Connection", template.getSubject());
        assertTrue(template.getMessage().contains("token-1"));
        assertEquals(List.of("user@example.com"), template.getRecipients());
    }
}
