package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateApplicationUseCaseTest {

    private ApplicationRepository applicationRepository;
    private UserAccountService userAccountService;
    private CreateApplicationUseCase createApplicationUseCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        userAccountService = mock(UserAccountService.class);
        createApplicationUseCase = new CreateApplicationUseCase(applicationRepository, userAccountService);
    }

    @Test
    void testCreateApplicationSuccess() {
        String name = "MyApp";
        String ownerEmail = "user@example.com";
        String accountKey = "account-123";

        when(applicationRepository.findByOwnerAndName(ownerEmail, name)).thenReturn(Optional.empty());
        when(userAccountService.getAccountKey(ownerEmail)).thenReturn(accountKey);
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationInfo result = createApplicationUseCase.create(name, ownerEmail);

        assertNotNull(result);
        assertEquals(name, result.name());
        assertEquals(ownerEmail, result.owner());
        assertEquals(accountKey, result.accountKey());

        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void testCreateApplicationNameAlreadyExists() {
        String name = "MyApp";
        String ownerEmail = "user@example.com";

        final Application app = Application.createNew(name, "key", ownerEmail);

        when(applicationRepository.findByOwnerAndName(ownerEmail, name))
                .thenReturn(Optional.of(app));

        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                createApplicationUseCase.create(name, ownerEmail)
        );

        assertEquals(Errors.APPLICATION_NAME_ALREADY_EXISTS, ex.getError());
        verify(applicationRepository, never()).save(any());
    }
}
