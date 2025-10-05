package com.langa.backend.domain.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.domain.teams.valueobjects.TeamRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class Team {

    private String id;
    private String name;
    private String key;
    private List<TeamMember> members;
    private String createdBy;
    private LocalDateTime createdDate;

    public Team() {}

    private Team(String name, String createdBy, LocalDateTime createdDate) {
        this.name = name;
        this.key = KeyGenerator.generateTeamKey(name, createdBy);
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.members = new ArrayList<>();
        this.members.add(new TeamMember(createdBy, TeamRole.OWNER, this.key, LocalDateTime.now()));
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
        return new TeamInvitation()
                .setTeam(key)
                .setHost(createdBy)
                .setGuest(guest)
                .setStatus(InvitationStatus.CREATED)
                .setInvitationToken(KeyGenerator.generateTeamInvitationKey(key, createdBy, guest, now.toString()))
                .setInviteDate(now)
                .setExpiryDate(now.plusDays(1));
    }

    public TeamMember addMember(String memberEmail) {
        boolean isAlreadyMember = members.stream()
                .anyMatch(teamMember -> Objects.equals(teamMember.email(), memberEmail));

        if(isAlreadyMember) {
            throw new TeamException("Already member of the team", null, Errors.TEAM_MEMBER_ALREADY);
        }
        final TeamMember teamMember = new TeamMember(memberEmail, TeamRole.MEMBER, key, LocalDateTime.now());
        members.add(teamMember);
        return teamMember;
    }
}
