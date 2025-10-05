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
        final Team team = teamRepository.findById(teamInvitation.getStakeHolders().team())
                .orElseThrow(() -> new TeamException("Team not found with id "+ teamInvitation.getStakeHolders().team(), null, Errors.TEAM_NOT_FOUND));

        team.checkOwnership(teamInvitation.getStakeHolders().host());
        teamInvitationRepository.findExistingValidInvitation(team.getKey(), teamInvitation.getStakeHolders().guest())
                .ifPresent(invitation -> {
                    throw new TeamException("Invitation found with status : "+invitation.getStatus().name(), null, Errors.TEAM_INVITATION_EXISTING);
                });

        final TeamInvitation invitation = teamInvitationRepository.save(team.invite(teamInvitation.getStakeHolders().guest()));

        TeamInvitationEmailEvent invitationEmailEvent = TeamInvitationEmailEvent.of(teamInvitation, team);

        outboxEventService.storeOutboxEvent(invitationEmailEvent);

        return invitation;
    }
}
