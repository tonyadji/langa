package com.langa.backend.domain.applications.usecases.fetch;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogQueryRepository;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLogUseCaseTest {

    @Mock
    private LogQueryRepository logRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private GetLogUseCase useCase;

    @Test
    void getFilteredLogs_shouldReturnPageResult_whenUserIsOwner() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getAllAccountKeys("owner@example.com")).thenReturn(Set.of("ACC-1"));
        PaginatedResult<LogEntry> page = new PaginatedResult<>(List.of(), 0, 0, 0, 100);
        when(logRepository.findFiltered(any(), any(LogFilter.class), anyInt(), anyInt())).thenReturn(page);

        PaginatedResult<LogEntry> result = useCase.getFilteredLogs("app-1", "owner@example.com",
                new LogFilter(null, null, null, null), 0, 100);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getFilteredLogs_shouldThrow_whenApplicationNotFound() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> useCase.getFilteredLogs("app-1", "owner@example.com", new LogFilter(null, null, null, null), 0, 100));

        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }

    @Test
    void getFilteredLogs_shouldThrow_whenUserHasNoAccess() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getAllAccountKeys("intruder@example.com")).thenReturn(Set.of("ACC-OTHER"));

        assertThrows(ApplicationException.class,
                () -> useCase.getFilteredLogs("app-1", "intruder@example.com", new LogFilter(null, null, null, null), 0, 100));
    }
}
