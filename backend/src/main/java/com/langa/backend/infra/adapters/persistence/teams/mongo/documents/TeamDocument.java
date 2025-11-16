package com.langa.backend.infra.adapters.persistence.teams.mongo.documents;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.valueobjects.TeamMember;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "c_teams")
@Data
public class TeamDocument {

    @Id
    private String id;
    private String name;
    private String key;
    private List<TeamMember> members;
    private String createdBy;
    private LocalDateTime createdDate;

    public Team toTeam() {
        final Team team = Team.populate(id, name, createdBy, createdDate);
        team.getMembers().addAll(members);
        return team;
    }

    public static TeamDocument of(Team team) {
        TeamDocument teamDocument = new TeamDocument();
        teamDocument.setId(team.getId());
        teamDocument.setName(team.getName());
        teamDocument.setKey(team.getKey());
        teamDocument.setMembers(team.getMembers());
        teamDocument.setCreatedBy(team.getCreatedBy());
        teamDocument.setCreatedDate(team.getCreatedDate());
        return teamDocument;
    }
}
