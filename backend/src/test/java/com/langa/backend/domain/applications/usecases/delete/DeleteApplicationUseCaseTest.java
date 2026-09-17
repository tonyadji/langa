package com.langa.backend.domain.applications.usecases.delete;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private DeleteApplicationUseCase useCase;

    @Test
    void execute_shouldDeleteApplication_whenOwnerMatches() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.of(app));

        useCase.execute(new DeleteApplicationCommand("app-1", "owner@example.com"));

        verify(applicationRepository).deleteById("app-1");
    }

    @Test
    void execute_shouldThrow_whenApplicationNotFound() {
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> useCase.execute(new DeleteApplicationCommand("app-1", "owner@example.com")));

        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
        verify(applicationRepository, never()).deleteById(anyString());
    }

    @Test
    void execute_shouldThrow_whenCallerIsNotOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "someone-else@example.com")).thenReturn(Optional.of(app));

        assertThrows(ApplicationException.class,
                () -> useCase.execute(new DeleteApplicationCommand("app-1", "someone-else@example.com")));

        verify(applicationRepository, never()).deleteById(anyString());
    }

    @Test
    void handle_shouldReturnSuccessMessage() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.securedFindByIdAndOwner("app-1", "owner@example.com")).thenReturn(Optional.of(app));

        String result = useCase.handle(new DeleteApplicationCommand("app-1", "owner@example.com"));

        assertEquals("App Deleted Successfully", result);
    }
}
