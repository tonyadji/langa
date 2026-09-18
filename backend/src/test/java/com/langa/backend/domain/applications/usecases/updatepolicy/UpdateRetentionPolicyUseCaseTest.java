package com.langa.backend.domain.applications.usecases.updatepolicy;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRetentionPolicyUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private UpdateRetentionPolicyUseCase useCase;

    @Test
    void execute_shouldUpdatePolicy_whenOwnerMatches() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = useCase.execute(new UpdateRetentionPolicyCommand("app-1", "owner@example.com", 10, ChronoUnit.DAYS));

        assertEquals(10, result.getRetentionPolicy().duration());
        assertEquals(ChronoUnit.DAYS, result.getRetentionPolicy().unit());
    }

    @Test
    void execute_shouldThrow_whenApplicationNotFound() {
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class, () -> useCase.execute(
                new UpdateRetentionPolicyCommand("app-1", "owner@example.com", 10, ChronoUnit.DAYS)));

        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenCallerIsNotOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "intruder@example.com")).thenReturn(Optional.of(app));

        assertThrows(ApplicationException.class, () -> useCase.execute(
                new UpdateRetentionPolicyCommand("app-1", "intruder@example.com", 10, ChronoUnit.DAYS)));
    }

    @Test
    void handle_shouldDelegateToExecute() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = useCase.handle(new UpdateRetentionPolicyCommand("app-1", "owner@example.com", 5, ChronoUnit.DAYS));

        assertEquals(5, result.getRetentionPolicy().duration());
    }
}
