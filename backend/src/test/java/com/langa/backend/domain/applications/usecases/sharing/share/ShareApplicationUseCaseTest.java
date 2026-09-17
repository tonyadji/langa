package com.langa.backend.domain.applications.usecases.sharing.share;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import com.langa.backend.domainexchange.teams.TeamService;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private TeamService teamService;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private ShareApplicationUseCase useCase;

    @Test
    void execute_shouldShareWithUser_whenValid() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getShareWithInfo("guest@example.com"))
                .thenReturn(new ShareWithInfo("ACC-GUEST", "guest@example.com"));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationInfo info = useCase.execute(
                new ShareApplicationCommand("app-1", "owner@example.com", "guest@example.com", SharedWithProfile.USER));

        assertEquals(1, info.sharedWith().size());
        verify(outboxEventService).storeOutboxEvent(any());
    }

    @Test
    void execute_shouldShareWithTeam_whenValid() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(teamService.getShareWithInfo("team-key")).thenReturn(new ShareWithInfo("team-key", "team@example.com"));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationInfo info = useCase.execute(
                new ShareApplicationCommand("app-1", "owner@example.com", "team-key", SharedWithProfile.TEAM));

        assertEquals(1, info.sharedWith().size());
    }

    @Test
    void execute_shouldThrow_whenSharingWithSelf() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getShareWithInfo("owner@example.com"))
                .thenReturn(new ShareWithInfo("ACC-1", "owner@example.com"));

        ApplicationException ex = assertThrows(ApplicationException.class, () -> useCase.execute(
                new ShareApplicationCommand("app-1", "owner@example.com", "owner@example.com", SharedWithProfile.USER)));

        assertEquals(Errors.APPLICATION_AUTO_SHARE_FORBIDDEN, ex.getError());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrow_whenAlreadyShared() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        app.shareWith("ACC-GUEST", SharedWithProfile.USER);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getShareWithInfo("guest@example.com"))
                .thenReturn(new ShareWithInfo("ACC-GUEST", "guest@example.com"));

        ApplicationException ex = assertThrows(ApplicationException.class, () -> useCase.execute(
                new ShareApplicationCommand("app-1", "owner@example.com", "guest@example.com", SharedWithProfile.USER)));

        assertEquals(Errors.APPLICATION_ALREADY_SHARED, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenApplicationNotFound() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> useCase.execute(
                new ShareApplicationCommand("app-1", "owner@example.com", "guest@example.com", SharedWithProfile.USER)));
    }

    @Test
    void handle_shouldDelegateToExecute() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getShareWithInfo("guest@example.com"))
                .thenReturn(new ShareWithInfo("ACC-GUEST", "guest@example.com"));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationInfo info = useCase.handle(
                new ShareApplicationCommand("app-1", "owner@example.com", "guest@example.com", SharedWithProfile.USER));

        assertEquals(1, info.sharedWith().size());
    }
}
