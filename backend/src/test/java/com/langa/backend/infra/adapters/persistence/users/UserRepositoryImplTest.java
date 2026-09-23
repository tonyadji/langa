package com.langa.backend.infra.adapters.persistence.users;

import com.langa.backend.domain.users.User;
import com.langa.backend.infra.adapters.persistence.users.mongo.MongoUserDao;
import com.langa.backend.infra.adapters.persistence.users.mongo.UserDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private MongoUserDao mongoUserDao;

    @InjectMocks
    private UserRepositoryImpl repository;

    private User user() {
        return User.createFromExternalIdentity("oid-1", "user@example.com");
    }

    @Test
    void save_shouldPersistAndReturnUser() {
        User user = user();
        when(mongoUserDao.save(any(UserDocument.class))).thenReturn(UserDocument.of(user));

        Optional<User> saved = repository.save(user);

        assertTrue(saved.isPresent());
        assertEquals("user@example.com", saved.get().getEmail());
        assertEquals("oid-1", saved.get().getExternalId());
    }

    @Test
    void findByEmail_shouldReturnUser_whenFound() {
        User user = user();
        when(mongoUserDao.findByEmail("user@example.com")).thenReturn(Optional.of(UserDocument.of(user)));

        assertTrue(repository.findByEmail("user@example.com").isPresent());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        when(mongoUserDao.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertTrue(repository.findByEmail("unknown@example.com").isEmpty());
    }

    @Test
    void findByExternalId_shouldReturnUser() {
        User user = user();
        when(mongoUserDao.findByExternalId("oid-1")).thenReturn(Optional.of(UserDocument.of(user)));

        assertTrue(repository.findByExternalId("oid-1").isPresent());
    }

    @Test
    void findByEmailIgnoreCase_shouldReturnUser() {
        User user = user();
        when(mongoUserDao.findFirstByEmailIgnoreCase("User@Example.com")).thenReturn(Optional.of(UserDocument.of(user)));

        assertTrue(repository.findByEmailIgnoreCase("User@Example.com").isPresent());
    }

    @Test
    void findByEmailOrAccountKey_shouldReturnUser() {
        User user = user();
        when(mongoUserDao.findByEmailOrAccountKey("user@example.com", "user@example.com"))
                .thenReturn(Optional.of(UserDocument.of(user)));

        assertTrue(repository.findByEmailOrAccountKey("user@example.com").isPresent());
    }
}
