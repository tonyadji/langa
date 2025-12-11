package com.langa.backend.domain.teams;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Team extends AbstractModel {

    private final TeamId teamId;
    private final String name;
    private final List<TeamMember> members;
    private final String createdBy;
    private final LocalDateTime createdDate;


    private Team(String name, String createdBy, LocalDateTime createdDate) {
        this.teamId = TeamId.of(name, createdBy);
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = new ArrayList<>();
        this.members.add(new TeamMember(createdBy, TeamRole.OWNER, this.teamId.key(), LocalDateTime.now()));
    }

    private Team(TeamId teamId, String name, String createdBy, List<TeamMember> members, LocalDateTime createdDate) {
        this.teamId = teamId;
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = members;
    }

    public static Team populate(TeamId teamId, String name, String createdBy, List<TeamMember> members, LocalDateTime createdDate) {
        return new Team(teamId, name, createdBy, members, createdDate);
    }

    public static Team createNew(String name, String createdBy, LocalDateTime createdDate) {
        return new Team(name, createdBy, createdDate);
    }

    public void checkOwnership(String host) {
        if (!Objects.equals(host, this.createdBy)) {
           throw new TeamException("Team Ownership", null, Errors.ACCESS_DENIED);
        }
    }

    public TeamInvitation invite(String guest) {
        boolean isAlreadyMember = members.stream()
                .anyMatch(teamMember -> Objects.equals(teamMember.email(), guest));

        if(isAlreadyMember) {
            throw new TeamException("Already member of the team", null, Errors.TEAM_MEMBER_ALREADY);
        }

        LocalDateTime now = LocalDateTime.now();

        return TeamInvitation.populate(null,
                new TeamInvitationStakeHolders(teamId.key(), createdBy, guest),
                new TeamInvitationPeriod(now, now.plusDays(1)),
                null,
                InvitationStatus.CREATED);
    }

    public void addMember(String memberEmail) {
        boolean isAlreadyMember = members.stream()
                .anyMatch(teamMember -> Objects.equals(teamMember.email(), memberEmail));

        if(isAlreadyMember) {
            throw new TeamException("Already member of the team", null, Errors.TEAM_MEMBER_ALREADY);
        }
        final TeamMember teamMember = new TeamMember(memberEmail, TeamRole.MEMBER, teamId.key(), LocalDateTime.now());
        members.add(teamMember);
    }

    public String getId() {
        return teamId.id();
    }

    public String getKey() {
        return teamId.key();
    }
}
