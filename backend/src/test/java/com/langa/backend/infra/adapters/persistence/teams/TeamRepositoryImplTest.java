package com.langa.backend.infra.adapters.persistence.teams;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.MongoTeamDao;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.MongoTeamMemberDao;
import com.langa.backend.infra.adapters.persistence.teams.mongo.documents.TeamDocument;
import com.langa.backend.infra.adapters.persistence.teams.mongo.documents.TeamMemberDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryImplTest {

    @Mock
    private MongoTeamDao mongoTeamDao;
    @Mock
    private MongoTeamMemberDao mongoTeamMemberDao;

    @InjectMocks
    private TeamRepositoryImpl repository;

    private Team team() {
        return Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
    }

    @Test
    void save_shouldPersistNewMembers_andClearThem() {
        Team team = team();
        team.addMember("member@example.com");
        when(mongoTeamDao.save(any(TeamDocument.class))).thenReturn(TeamDocument.of(team));

        Team saved = repository.save(team);

        assertNotNull(saved);
        verify(mongoTeamMemberDao).saveAll(anyList());
        assertTrue(team.getNewMembers().isEmpty());
    }

    @Test
    void save_shouldSkipMemberPersistence_whenNoNewMembers() {
        Team team = team();
        when(mongoTeamDao.save(any(TeamDocument.class))).thenReturn(TeamDocument.of(team));

        repository.save(team);

        verifyNoInteractions(mongoTeamMemberDao);
    }

    @Test
    void findByOwnerAndName_shouldReturnTeam() {
        Team team = team();
        when(mongoTeamDao.findByCreatedByAndName("owner@example.com", "Dev Team")).thenReturn(Optional.of(TeamDocument.of(team)));

        assertTrue(repository.findByOwnerAndName("owner@example.com", "Dev Team").isPresent());
    }

    @Test
    void findByKey_shouldReturnTeam() {
        Team team = team();
        when(mongoTeamDao.findByKey(team.getKey())).thenReturn(Optional.of(TeamDocument.of(team)));

        assertTrue(repository.findByKey(team.getKey()).isPresent());
    }

    @Test
    void findById_shouldReturnTeam() {
        Team team = team();
        when(mongoTeamDao.findById(team.getId())).thenReturn(Optional.of(TeamDocument.of(team)));

        assertTrue(repository.findById(team.getId()).isPresent());
    }

    @Test
    void findByOwner_shouldMapDocuments() {
        Team team = team();
        when(mongoTeamDao.findByCreatedBy("owner@example.com")).thenReturn(List.of(TeamDocument.of(team)));

        assertEquals(1, repository.findByOwner("owner@example.com").size());
    }

    @Test
    void findTeamsKeysByMemberUsername_shouldReturnEmptySet() {
        assertTrue(repository.findTeamsKeysByMemberUsername("owner@example.com").isEmpty());
    }

    @Test
    void findByOwnerOrTeamMember_shouldCombineOwnedAndSharedTeams() {
        Team ownedTeam = team();
        Team sharedTeam = Team.createNew("Other Team", "someone-else@example.com", LocalDateTime.now());
        TeamMemberDocument memberDocument = new TeamMemberDocument();
        memberDocument.setTeamKey(sharedTeam.getKey());

        when(mongoTeamDao.findByCreatedBy("owner@example.com")).thenReturn(List.of(TeamDocument.of(ownedTeam)));
        when(mongoTeamMemberDao.findByEmail("owner@example.com")).thenReturn(List.of(memberDocument));
        when(mongoTeamDao.findByKeyIn(List.of(sharedTeam.getKey()))).thenReturn(List.of(TeamDocument.of(sharedTeam)));

        List<Team> result = repository.findByOwnerOrTeamMember("owner@example.com");

        assertEquals(2, result.size());
    }
}
