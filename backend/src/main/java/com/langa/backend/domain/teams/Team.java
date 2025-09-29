package com.langa.backend.domain.teams;

import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.domain.teams.valueobjects.TeamRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
}
