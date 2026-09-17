package com.langa.backend.domain.teams.usecases.invitations.accept;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationUseCaseTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private AcceptInvitationUseCase useCase;

    @Test
    void execute_shouldAcceptInvitation_whenValid() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamInvitation result = useCase.execute(new AcceptInvitationCommand(team.getId(), token, "guest@example.com"));

        assertNotNull(result);
        assertEquals("guest@example.com", result.getStakeHolders().guest());
    }

    @Test
    void execute_shouldThrow_whenTeamNotFound() {
        when(teamRepository.findById("team-1")).thenReturn(Optional.empty());

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.execute(new AcceptInvitationCommand("team-1", "token", "guest@example.com")));

        assertEquals(Errors.TEAM_NOT_FOUND, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenInvitationNotFound() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.execute(new AcceptInvitationCommand(team.getId(), "unknown-token", "guest@example.com")));

        assertEquals(Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenGuestDoesNotMatch() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.execute(new AcceptInvitationCommand(team.getId(), token, "someone-else@example.com")));

        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenInvitationExpired() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);
        // force expiry by manipulating status through markAsExpired via team API
        team.flagInvitationExpired(invitation);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThrows(TeamException.class,
                () -> useCase.execute(new AcceptInvitationCommand(team.getId(), invitation.getToken(), "guest@example.com")));
    }

    @Test
    void handle_shouldDelegateToExecute() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamInvitation result = useCase.handle(new AcceptInvitationCommand(team.getId(), token, "guest@example.com"));

        assertNotNull(result);
    }
}
