package com.langa.backend.domain.applications.usecases.create;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private CreateApplicationUseCase useCase;

    @Test
    void execute_shouldCreateApplication_whenNameNotAlreadyUsed() {
        when(applicationRepository.findByOwnerAndName("owner@example.com", "My App")).thenReturn(Optional.empty());
        when(userAccountService.getAccountKey("owner@example.com")).thenReturn("ACC-1");
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationInfo info = useCase.execute(new CreateApplicationCommand("My App", "owner@example.com"));

        assertEquals("My App", info.name());
        assertEquals("owner@example.com", info.owner());
        assertEquals("ACC-1", info.accountKey());
        verify(outboxEventService, times(1)).storeOutboxEvent(any());
    }

    @Test
    void execute_shouldThrow_whenNameAlreadyExistsForOwner() {
        Application existing = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findByOwnerAndName("owner@example.com", "My App")).thenReturn(Optional.of(existing));

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> useCase.execute(new CreateApplicationCommand("My App", "owner@example.com")));

        assertEquals(Errors.APPLICATION_NAME_ALREADY_EXISTS, ex.getError());
        verify(applicationRepository, never()).save(any());
        verifyNoInteractions(outboxEventService);
    }

    @Test
    void handle_shouldDelegateToExecute() {
        when(applicationRepository.findByOwnerAndName(any(), any())).thenReturn(Optional.empty());
        when(userAccountService.getAccountKey(any())).thenReturn("ACC-1");
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationInfo info = useCase.handle(new CreateApplicationCommand("My App", "owner@example.com"));

        assertEquals("My App", info.name());
    }
}
