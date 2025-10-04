package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationUseCaseTest {

    @InjectMocks
    private AcceptInvitationUseCase acceptInvitationUseCase;

    @Mock
    private TeamInvitationRepository teamInvitationRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Test
    void shouldAcceptInvitationWhenIdExists() {
        String invitationId = "valid-invitation-id";
        final TeamInvitation validInvitation = new TeamInvitation()
                .setId(invitationId)
                .setStatus(InvitationStatus.CREATED)
                .setExpiryDate(LocalDateTime.now().plusDays(1));
        when(teamInvitationRepository.findById(invitationId))
                .thenReturn(Optional.of(validInvitation));
        when(teamInvitationRepository.save(Mockito.any(TeamInvitation.class))).thenReturn(validInvitation);

        acceptInvitationUseCase.acceptInvitation(invitationId);

        assertEquals(InvitationStatus.ACCEPTED, validInvitation.getStatus());
        verify(teamInvitationRepository, times(1)).findById(invitationId);
        verify(teamInvitationRepository, times(1)).save(Mockito.any(TeamInvitation.class));
        verify(outboxEventService, times(2)).storeOutboxEvent(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenInvitationNotFound() {
        String invalidInvitationId = "invalid-invitation-id";
        when(teamInvitationRepository.findById(invalidInvitationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> acceptInvitationUseCase.acceptInvitation(invalidInvitationId))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("Invitation not found");

        verify(teamInvitationRepository, times(1)).findById(invalidInvitationId);
        verify(teamInvitationRepository, never()).save(Mockito.any());
        verify(outboxEventService, never()).storeOutboxEvent(Mockito.any(DomainEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenInvitationExpired() {
        String invalidInvitationId = "expired-invitation-id";
        final TeamInvitation expiredInvitation = new TeamInvitation()
                .setId(invalidInvitationId)
                .setExpiryDate(LocalDateTime.now().minusDays(1));
        when(teamInvitationRepository.findById(invalidInvitationId)).thenReturn(Optional.of(expiredInvitation));

        assertThatThrownBy(() -> acceptInvitationUseCase.acceptInvitation(invalidInvitationId))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("Invalid status");

        verify(teamInvitationRepository, times(1)).findById(invalidInvitationId);
        verify(teamInvitationRepository, never()).save(Mockito.any());
        verify(outboxEventService, never()).storeOutboxEvent(Mockito.any(DomainEvent.class));
    }

}