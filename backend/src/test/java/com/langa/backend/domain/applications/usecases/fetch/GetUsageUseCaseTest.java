package com.langa.backend.domain.applications.usecases.fetch;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUsageUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private GetUsageUseCase useCase;

    @Test
    void getApplicationUsage_shouldReturnUsageInfo_whenOwnerMatches() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(applicationRepository.findApplicationUsageTrends(app.getKey())).thenReturn(Collections.emptyList());

        ApplicationUsageInfo info = useCase.getApplicationUsage("app-1", "owner@example.com");

        assertEquals(app.getId(), info.id());
        assertEquals(app.getKey(), info.key());
        assertEquals(0L, info.logSize());
        assertEquals(0L, info.metricSize());
    }

    @Test
    void getApplicationUsage_shouldThrow_whenApplicationNotFound() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> useCase.getApplicationUsage("app-1", "owner@example.com"));

        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }

    @Test
    void getApplicationUsage_shouldThrow_whenCallerIsNotOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));

        assertThrows(ApplicationException.class,
                () -> useCase.getApplicationUsage("app-1", "someone-else@example.com"));
    }
}
