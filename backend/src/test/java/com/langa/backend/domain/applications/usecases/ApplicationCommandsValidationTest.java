package com.langa.backend.domain.applications.usecases;

import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.usecases.create.CreateApplicationCommand;
import com.langa.backend.domain.applications.usecases.delete.DeleteApplicationCommand;
import com.langa.backend.domain.applications.usecases.sharing.revoke.RevokeSharingApplicationCommand;
import com.langa.backend.domain.applications.usecases.sharing.share.ShareApplicationCommand;
import com.langa.backend.domain.applications.usecases.updatepolicy.UpdateRetentionPolicyCommand;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationCommandsValidationTest {

    @Test
    void createApplicationCommand_shouldRejectBlankNameOrOwner() {
        assertThrows(ApplicationException.class, () -> new CreateApplicationCommand("", "owner@example.com"));
        assertThrows(ApplicationException.class, () -> new CreateApplicationCommand("name", ""));
    }

    @Test
    void deleteApplicationCommand_shouldRejectBlankAppIdOrOwner() {
        assertThrows(ApplicationException.class, () -> new DeleteApplicationCommand(" ", "owner@example.com"));
        assertThrows(ApplicationException.class, () -> new DeleteApplicationCommand("app-1", " "));
    }

    @Test
    void shareApplicationCommand_shouldRejectMissingFields() {
        assertThrows(ApplicationException.class, () -> new ShareApplicationCommand(" ", "owner", "guest", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new ShareApplicationCommand("app-1", " ", "guest", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new ShareApplicationCommand("app-1", "owner", " ", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new ShareApplicationCommand("app-1", "owner", "guest", null));
    }

    @Test
    void revokeSharingApplicationCommand_shouldRejectMissingFields() {
        assertThrows(ApplicationException.class, () -> new RevokeSharingApplicationCommand(" ", "owner", "guest", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new RevokeSharingApplicationCommand("app-1", " ", "guest", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new RevokeSharingApplicationCommand("app-1", "owner", " ", SharedWithProfile.USER));
        assertThrows(ApplicationException.class, () -> new RevokeSharingApplicationCommand("app-1", "owner", "guest", null));
    }

    @Test
    void updateRetentionPolicyCommand_shouldRejectInvalidFields() {
        assertThrows(ApplicationException.class, () -> new UpdateRetentionPolicyCommand(" ", "owner", 1, ChronoUnit.DAYS));
        assertThrows(ApplicationException.class, () -> new UpdateRetentionPolicyCommand("app-1", " ", 1, ChronoUnit.DAYS));
        assertThrows(ApplicationException.class, () -> new UpdateRetentionPolicyCommand("app-1", "owner", 0, ChronoUnit.DAYS));
        assertThrows(ApplicationException.class, () -> new UpdateRetentionPolicyCommand("app-1", "owner", 1, null));
    }

    @Test
    void updateRetentionPolicyCommand_toRetentionPolicy_shouldBuildPolicy() {
        UpdateRetentionPolicyCommand command = new UpdateRetentionPolicyCommand("app-1", "owner", 5, ChronoUnit.DAYS);
        var policy = command.toRetentionPolicy();
        assertEquals(5, policy.duration());
        assertEquals(ChronoUnit.DAYS, policy.unit());
    }
}
