package com.langa.backend.infra.persistence.repositories.teams.mongo.documents;

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
        return new Team()
                .setId(id)
                .setName(name)
                .setKey(key)
                .setMembers(members)
                .setCreatedBy(createdBy)
                .setCreatedDate(createdDate);
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
