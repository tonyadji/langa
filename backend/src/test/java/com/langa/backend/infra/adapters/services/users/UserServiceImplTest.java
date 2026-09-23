package com.langa.backend.infra.adapters.services.users;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void provisionExternalUser_shouldReturnLinkedUser_whenAlreadyKnown() {
        User known = User.createFromExternalIdentity("oid-1", "user@example.com");
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.of(known));

        User result = userService.provisionExternalUser("oid-1", "user@example.com");

        assertSame(known, result);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(outboxEventService);
    }

    @Test
    void provisionExternalUser_shouldLinkLegacyUserByEmail_keepingItsAccountKey() {
        User legacy = User.populate(UserId.of("id-1", "User@Example.com", "acc-1"), null, UserStatus.ACTIVE);
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(legacy));
        when(userRepository.save(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        User result = userService.provisionExternalUser("oid-1", "user@example.com");

        assertEquals("acc-1", result.getAccountKey());
        assertEquals("User@Example.com", result.getEmail());
        assertEquals("oid-1", result.getExternalId());
        verifyNoInteractions(outboxEventService);
    }

    @Test
    void provisionExternalUser_shouldActivateInvitedUser() {
        User invited = User.createInvited("guest@example.com");
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("guest@example.com")).thenReturn(Optional.of(invited));
        when(userRepository.save(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        User result = userService.provisionExternalUser("oid-1", "guest@example.com");

        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertEquals(invited.getAccountKey(), result.getAccountKey());
    }

    @Test
    void provisionExternalUser_shouldCreateUser_whenUnknown() {
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("New@Example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        User result = userService.provisionExternalUser("oid-1", "New@Example.com");

        assertEquals("new@example.com", result.getEmail());
        assertEquals("oid-1", result.getExternalId());
        assertNotNull(result.getAccountKey());
        verify(outboxEventService).storeOutboxEvent(any(ActiveUserRegisteredEvent.class));
    }

    @Test
    void provisionExternalUser_shouldRefuse_whenEmailLinkedToAnotherIdentity() {
        User other = User.createFromExternalIdentity("oid-other", "user@example.com");
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(other));

        assertThrows(UserException.class, () -> userService.provisionExternalUser("oid-1", "user@example.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void provisionExternalUser_shouldReturnConcurrentlyProvisionedUser_onDuplicateKey() {
        User concurrent = User.createFromExternalIdentity("oid-1", "user@example.com");
        when(userRepository.findByExternalId("oid-1")).thenReturn(Optional.empty(), Optional.of(concurrent));
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenThrow(new DuplicateKeyException("dup"));

        User result = userService.provisionExternalUser("oid-1", "user@example.com");

        assertSame(concurrent, result);
    }

    @Test
    void findOrCreateUserByEmail_shouldCreateInvitedUser_whenUnknown() {
        when(userRepository.findByEmail("guest@example.com")).thenReturn(Optional.empty());

        User result = userService.findOrCreateUserByEmail("guest@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(UserStatus.CREATED, captor.getValue().getStatus());
        assertFalse(result.isLinked());
    }
}
