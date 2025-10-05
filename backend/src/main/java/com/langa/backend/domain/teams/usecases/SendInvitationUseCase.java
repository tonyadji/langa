package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class SendInvitationUseCase {

    private final TeamRepository teamRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final OutboxEventService outboxEventService;


    public TeamInvitation invite(TeamInvitation teamInvitation) {
        final Team team = teamRepository.findById(teamInvitation.getTeam())
                .orElseThrow(() -> new TeamException("Team not found with id "+ teamInvitation.getTeam(), null, Errors.TEAM_NOT_FOUND));

        team.checkOwnership(teamInvitation.getHost());
        teamInvitationRepository.findExistingValidInvitation(team.getKey(), teamInvitation.getGuest())
                .ifPresent(invitation -> {
                    throw new TeamException("Invitation found with status : "+invitation.getStatus().name(), null, Errors.TEAM_INVITATION_EXISTING);
                });

        final TeamInvitation invitation = teamInvitationRepository.save(team.invite(teamInvitation.getGuest()));

        TeamInvitationEmailEvent invitationEmailEvent = TeamInvitationEmailEvent.of(teamInvitation, team);

        outboxEventService.storeOutboxEvent(invitationEmailEvent);

        return invitation;
    }
}
