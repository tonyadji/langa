package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import org.junit.jupiter.api.BeforeEach;
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
class SendInvitationUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamInvitationRepository teamInvitationRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private SendInvitationUseCase useCase;

    @Mock
    private Team team;

    private TeamInvitation invitation;

    @BeforeEach
    void setup() {
        invitation = new TeamInvitation();
        invitation.setTeam("team123");
        invitation.setGuest("guest@example.com");
        invitation.setHost("host@example.com");
    }

    @Test
    void invite_shouldSaveInvitationAndStoreEvent() {
        when(teamRepository.findById("team123")).thenReturn(Optional.of(team));
        when(team.invite("guest@example.com")).thenReturn(invitation);
        when(teamInvitationRepository.findExistingValidInvitation(any(), any())).thenReturn(Optional.empty());
        when(teamInvitationRepository.save(any())).thenReturn(invitation);
        when(team.getKey()).thenReturn("team123-key");
        when(team.getName()).thenReturn("Dream Team");

        TeamInvitation result = useCase.invite(invitation);

        assertNotNull(result);
        verify(teamRepository).findById("team123");
        verify(teamInvitationRepository).save(invitation);
        verify(outboxEventService).storeOutboxEvent(any(TeamInvitationEmailEvent.class));
        verify(team).checkOwnership("host@example.com");
    }

    @Test
    void invite_shouldThrowException_whenTeamNotFound() {
        when(teamRepository.findById("team123")).thenReturn(Optional.empty());

        TeamException ex = assertThrows(TeamException.class, () -> useCase.invite(invitation));
        assertEquals(Errors.TEAM_NOT_FOUND, ex.getError());

        verify(teamRepository).findById("team123");
        verifyNoInteractions(teamInvitationRepository, outboxEventService);
    }

    @Test
    void invite_shouldThrowException_whenExistingInvitationFound() {
        when(teamRepository.findById("team123")).thenReturn(Optional.of(team));
        when(team.getKey()).thenReturn("team123-key");
        final TeamInvitation teamInvitation = new TeamInvitation()
                .setStatus(InvitationStatus.CREATED);
        when(teamInvitationRepository.findExistingValidInvitation("team123-key", "guest@example.com"))
                .thenReturn(Optional.of(teamInvitation));

        TeamException ex = assertThrows(TeamException.class, () -> useCase.invite(invitation));
        assertEquals(Errors.TEAM_INVITATION_EXISTING, ex.getError());

        verify(teamRepository).findById("team123");
        verify(teamInvitationRepository).findExistingValidInvitation(any(), any());
        verify(team).checkOwnership("host@example.com");
        verifyNoMoreInteractions(teamInvitationRepository);
        verifyNoInteractions(outboxEventService);
    }

}