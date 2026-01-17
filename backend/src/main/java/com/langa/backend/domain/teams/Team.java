package com.langa.backend.domain.teams;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.langa.backend.domain.teams.valueobjects.InvitationStatus.*;

@Getter
public class Team extends AbstractModel {

    private final TeamId teamId;
    private final String name;
    private final List<TeamMember> members;
    private final List<TeamInvitation> invitations;
    private final String createdBy;
    private final LocalDateTime createdDate;

    private List<TeamMember> newMembers = new ArrayList<>();


    private Team(String name, String createdBy, LocalDateTime createdDate) {
        this.teamId = TeamId.newTeamIdOf(name, createdBy);
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = new ArrayList<>();
        this.invitations = new ArrayList<>();
        this.members.add(new TeamMember(createdBy, TeamRole.OWNER, this.teamId.key(), LocalDateTime.now()));
    }

    private Team(TeamId teamId, String name, String createdBy, List<TeamMember> members, List<TeamInvitation> invitations, LocalDateTime createdDate) {
        this.teamId = teamId;
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = members != null? members : new ArrayList<>();
        this.invitations = invitations != null? invitations : new ArrayList<>();
    }

    public static Team populate(TeamId teamId, String name, String createdBy, List<TeamMember> members, List<TeamInvitation> invitations, LocalDateTime createdDate) {
        return new Team(teamId, name, createdBy, members, invitations, createdDate);
    }

    public static Team createNew(String name, String createdBy, LocalDateTime createdDate) {
        return new Team(name, createdBy, createdDate);
    }

    public void checkOwnership(String owner) {
        boolean isTeamMember = this.members.stream().anyMatch(teamMember -> Objects.equals(teamMember.email(), owner));
        if (!Objects.equals(owner, this.createdBy) && !isTeamMember) {
           throw new TeamException("Team Ownership", null, Errors.ACCESS_DENIED);
        }
    }

    public void invite(String guest) {
        boolean isAlreadyMember = members.stream()
                .anyMatch(teamMember -> Objects.equals(teamMember.email(), guest));

        if(isAlreadyMember) {
            throw new TeamException("Already member of the team", null, Errors.TEAM_MEMBER_ALREADY);
        }

        boolean hasAlreadyValidInvitation = invitations.stream()
                .filter(invitation -> Objects.equals(invitation.getStakeHolders().guest(), guest))
                .anyMatch(teamInvitation -> List.of(CREATED, SENT, ACCEPTED).contains(teamInvitation.getStatus()));
        if(hasAlreadyValidInvitation) {
            throw new TeamException("Has already a valid invitation", null, Errors.TEAM_INVITATION_EXISTING);
        }
        LocalDateTime now = LocalDateTime.now();
        TeamInvitation teamInvitation = TeamInvitation.populate(TeamInvitationIdentity.of(teamId),
                new TeamInvitationStakeHolders(teamId.key(), createdBy, guest),
                new TeamInvitationPeriod(now, now.plusDays(1)),
                null,
                CREATED);

        this.invitations.add(teamInvitation);
        registerDomainEvent(TeamInvitationEmailEvent.of(teamInvitation, this));
    }

    public void addMember(String memberEmail) {
        boolean isAlreadyMember = members.stream()
                .anyMatch(teamMember -> Objects.equals(teamMember.email(), memberEmail));

        if(isAlreadyMember) {
            throw new TeamException("Already member of the team", null, Errors.TEAM_MEMBER_ALREADY);
        }
        final TeamMember teamMember = new TeamMember(memberEmail, TeamRole.MEMBER, teamId.key(), LocalDateTime.now());
        members.add(teamMember);
        newMembers.add(teamMember);
    }

    public String getId() {
        return teamId.id();
    }

    public String getKey() {
        return teamId.key();
    }

    public void flagInvitationExpired(TeamInvitation teamInvitation) {
        teamInvitation.markAsExpired();
        updateInvitation(teamInvitation);
    }

    public boolean acceptInvitation(TeamInvitation invitation) {
        if (invitation == null) {
            return false;
        }

        if(!invitation.isExpired()) {
            updateInvitation(invitation.accept());
            registerDomainEvent(TeamInvitationAcceptedByGuestEvent.of(invitation));
            registerDomainEvent(TeamInvitationAcceptedForHostEvent.of(invitation));
            return true;
        } else {
            invitation.markAsExpired();
            updateInvitation(invitation);
        }
        return false;
    }

    public TeamInvitation getInvitation(String token) {
        return invitations.stream()
                .filter(invitation -> Objects.equals(invitation.getIdentity().invitationToken(), token))
                .findFirst().orElse(null);
    }

    private void updateInvitation(TeamInvitation teamInvitation) {
        invitations.removeIf(invitation -> Objects.equals(invitation.getIdentity().invitationToken(), teamInvitation.getIdentity().invitationToken()));
        invitations.add(teamInvitation);
    }
}
