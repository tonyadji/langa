package com.langa.backend.infra.adapters.persistence.teams;

import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.domain.teams.valueobjects.TeamRole;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.MongoTeamMemberDao;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamMemberRepositoryImplTest {

    @Mock
    private MongoTeamMemberDao mongoTeamMemberDao;

    @InjectMocks
    private TeamMemberRepositoryImpl repository;

    @Test
    void save_shouldReturnExisting_whenAlreadyPresent() {
        TeamMember member = new TeamMember("member@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        when(mongoTeamMemberDao.findByEmailAndTeamKey("member@example.com", "team-key"))
                .thenReturn(Optional.of(TeamMemberDocument.of(member)));

        TeamMember result = repository.save(member);

        assertEquals("member@example.com", result.email());
        verify(mongoTeamMemberDao, never()).save(any());
    }

    @Test
    void save_shouldPersist_whenNotAlreadyPresent() {
        TeamMember member = new TeamMember("member@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        when(mongoTeamMemberDao.findByEmailAndTeamKey("member@example.com", "team-key")).thenReturn(Optional.empty());
        when(mongoTeamMemberDao.save(any(TeamMemberDocument.class))).thenReturn(TeamMemberDocument.of(member));

        TeamMember result = repository.save(member);

        assertEquals("member@example.com", result.email());
        verify(mongoTeamMemberDao).save(any());
    }

    @Test
    void findByEmail_shouldMapDocuments() {
        TeamMember member = new TeamMember("member@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        when(mongoTeamMemberDao.findByEmail("member@example.com")).thenReturn(List.of(TeamMemberDocument.of(member)));

        assertEquals(1, repository.findByEmail("member@example.com").size());
    }

    @Test
    void saveAll_shouldSaveEachMember() {
        TeamMember member1 = new TeamMember("m1@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        TeamMember member2 = new TeamMember("m2@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        when(mongoTeamMemberDao.findByEmailAndTeamKey(any(), any())).thenReturn(Optional.empty());
        when(mongoTeamMemberDao.save(any(TeamMemberDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        repository.saveAll(List.of(member1, member2));

        verify(mongoTeamMemberDao, times(2)).save(any());
    }

    @Test
    void findTeamsKeysByMemberUsername_shouldReturnDistinctTeamKeys() {
        TeamMember member = new TeamMember("member@example.com", TeamRole.MEMBER, "team-key", LocalDateTime.now());
        when(mongoTeamMemberDao.findByEmail("member@example.com")).thenReturn(List.of(TeamMemberDocument.of(member)));

        assertEquals(1, repository.findTeamsKeysByMemberUsername("member@example.com").size());
    }
}
