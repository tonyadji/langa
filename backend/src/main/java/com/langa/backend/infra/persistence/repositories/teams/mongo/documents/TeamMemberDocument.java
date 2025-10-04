package com.langa.backend.infra.persistence.repositories.teams.mongo.documents;

import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.domain.teams.valueobjects.TeamRole;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "c_team_members")
@Data
public class TeamMemberDocument {
    @Id
    private String id;
    private String email;
    private String teamKey;
    private TeamRole role;
    private LocalDateTime addedDate;

    public TeamMember toTeamMember() {
        return new TeamMember(email, role, teamKey, addedDate);
    }

    public static TeamMemberDocument of(TeamMember teamMember) {
        final TeamMemberDocument teamMemberDocument = new TeamMemberDocument();
        teamMemberDocument.setEmail(teamMember.email());
        teamMemberDocument.setTeamKey(teamMember.teamKey());
        teamMemberDocument.setRole(teamMember.role());
        teamMemberDocument.setAddedDate(teamMember.addedDate());
        return teamMemberDocument;
    }
}
