package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;

@UseCase
public class GetInvitationUseCase {

    private final TeamInvitationRepository teamInvitationRepository;

    public GetInvitationUseCase(TeamInvitationRepository teamInvitationRepository) {
        this.teamInvitationRepository = teamInvitationRepository;
    }

    public TeamInvitation getInvitation(String invitationToken) {
        final TeamInvitation teamInvitation = teamInvitationRepository.findByToken(invitationToken)
                .orElseThrow(() -> new TeamException("Invitation not found", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED));

        if(teamInvitation.isExpired()) {
            teamInvitation.markAsExpired();
        }
        teamInvitationRepository.save(teamInvitation);
        return teamInvitation;
    }
}
