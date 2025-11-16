package com.langa.backend.domain.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Team {

    private final String id;
    private final String name;
    private final String key;
    private final List<TeamMember> members;
    private final String createdBy;
    private final LocalDateTime createdDate;


    private Team(String id, String name, String createdBy, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.key = KeyGenerator.generateTeamKey(name, createdBy);
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = new ArrayList<>();
        this.members.add(new TeamMember(createdBy, TeamRole.OWNER, this.key, LocalDateTime.now()));
    }

    public static Team populate(String id, String name, String createdBy, LocalDateTime createdDate) {
        return new Team(id, name, createdBy, createdDate);
    }

    public static Team createNew(String name, String createdBy, LocalDateTime createdDate) {
        return new Team(null, name, createdBy, createdDate);
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
                new TeamInvitationStakeHolders(key, createdBy, guest),
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
        final TeamMember teamMember = new TeamMember(memberEmail, TeamRole.MEMBER, key, LocalDateTime.now());
        members.add(teamMember);
    }
}
