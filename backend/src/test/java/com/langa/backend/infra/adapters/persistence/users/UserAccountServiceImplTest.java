package com.langa.backend.infra.adapters.persistence.users;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.repositories.TeamMemberRepository;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private TeamRepository teamRepository;

    private UserAccountServiceImpl service;

    private User user() {
        return User.createFromExternalIdentity(new ExternalIdentity("entra", "oid-1", "user@example.com"));
    }

    private UserAccountServiceImpl newService() {
        return new UserAccountServiceImpl(userRepository, teamMemberRepository, teamRepository);
    }

    @Test
    void getAccountKey_shouldReturnAccountKey_whenUserFound() {
        service = newService();
        User user = user();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertEquals(user.getAccountKey(), service.getAccountKey("user@example.com"));
    }

    @Test
    void getAccountKey_shouldThrow_whenUserNotFound() {
        service = newService();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> service.getAccountKey("unknown@example.com"));
        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void getTeamKeys_shouldDelegateToTeamMemberRepository() {
        service = newService();
        when(teamMemberRepository.findTeamsKeysByMemberUsername("user@example.com")).thenReturn(Set.of("team-1"));

        assertEquals(Set.of("team-1"), service.getTeamKeys("user@example.com"));
    }

    @Test
    void getAllAccountKeys_shouldCombineUserAndTeamKeys() {
        service = newService();
        User user = user();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(teamMemberRepository.findTeamsKeysByMemberUsername("user@example.com")).thenReturn(Set.of("team-1"));

        Set<String> result = service.getAllAccountKeys("user@example.com");

        assertTrue(result.contains(user.getAccountKey()));
        assertTrue(result.contains("team-1"));
    }

    @Test
    void getAllAccountKeys_shouldThrow_whenUserNotFound() {
        service = newService();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> service.getAllAccountKeys("unknown@example.com"));
    }

    @Test
    void getShareWithInfo_shouldReturnInfo_whenFound() {
        service = newService();
        User user = user();
        when(userRepository.findByEmailOrAccountKey("user@example.com")).thenReturn(Optional.of(user));

        ShareWithInfo info = service.getShareWithInfo("user@example.com");

        assertEquals(user.getAccountKey(), info.key());
        assertEquals(user.getEmail(), info.email());
    }

    @Test
    void getShareWithInfo_shouldThrow_whenNotFound() {
        service = newService();
        when(userRepository.findByEmailOrAccountKey("unknown")).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> service.getShareWithInfo("unknown"));
    }
}
