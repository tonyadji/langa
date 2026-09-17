package com.langa.backend.domain.teams.usecases.invitations.fetch;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
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
class GetInvitationUseCaseTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private GetInvitationUseCase useCase;

    @Test
    void query_shouldReturnInvitation_whenVisibleByGuest() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamInvitation result = useCase.query(new GetInvitationQuery(team.getId(), token, "guest@example.com"));

        assertNotNull(result);
    }

    @Test
    void query_shouldThrow_whenTeamNotFound() {
        when(teamRepository.findById("team-1")).thenReturn(Optional.empty());

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.query(new GetInvitationQuery("team-1", "token", "guest@example.com")));

        assertEquals(Errors.TEAM_NOT_FOUND, ex.getError());
    }

    @Test
    void query_shouldThrow_whenInvitationNotFound() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.query(new GetInvitationQuery(team.getId(), "unknown", "guest@example.com")));

        assertEquals(Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED, ex.getError());
    }

    @Test
    void query_shouldThrow_whenNotVisibleByRequester() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.query(new GetInvitationQuery(team.getId(), token, "stranger@example.com")));

        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void query_shouldThrow_whenInvitationExpired() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        TeamInvitation expired = TeamInvitation.populate(
                new TeamInvitationIdentity(team.getId(), "expired-token"),
                new TeamInvitationStakeHolders(team.getKey(), "owner@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)),
                null, InvitationStatus.SENT);
        team.getInvitations().add(expired);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.query(new GetInvitationQuery(team.getId(), "expired-token", "guest@example.com")));

        assertEquals(Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED, ex.getError());
    }

    @Test
    void query_public_shouldReturnInvitation_withoutVisibilityCheck() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamInvitation result = useCase.query(new GetPublicInvitationQuery(team.getId(), token));

        assertNotNull(result);
    }

    @Test
    void query_public_shouldThrow_whenTeamNotFound() {
        when(teamRepository.findById("team-1")).thenReturn(Optional.empty());

        assertThrows(TeamException.class, () -> useCase.query(new GetPublicInvitationQuery("team-1", "token")));
    }

    @Test
    void query_public_shouldThrow_whenInvitationNotFound() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThrows(TeamException.class, () -> useCase.query(new GetPublicInvitationQuery(team.getId(), "unknown")));
    }
}
