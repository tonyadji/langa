package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.LogEntryRepository;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLogUseCaseTest {

    @Mock
    private LogEntryRepository logRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private GetLogUseCase getLogUseCase;

    private Application app;

    @BeforeEach
    void setUp() {
        app = Application.createNew("appKey", "accountKey", "owner@example.com");
    }

    @Test
    void getLogs_shouldReturnLogs() {
        LogEntry log = new LogEntry().setMessage("test");
        when(applicationRepository.findById("appId")).thenReturn(Optional.of(app));
        when(logRepository.findByAppKeyAndAccountKeyOrderByTimestampDesc(any(), any()))
                .thenReturn(List.of(log));

        List<LogEntry> result = getLogUseCase.getLogs("appId");
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getMessage());
    }

    @Test
    void getLogs_appNotFound_shouldThrowException() {
        when(applicationRepository.findById("appId")).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> getLogUseCase.getLogs("appId"));
        assertEquals(Errors.APPLICATION_NOT_FOUND, ex.getError());
    }

    @Test
    void getLogs_withUsername_wrongOwner_shouldThrowAccessDenied() {
        when(applicationRepository.findById("appId")).thenReturn(Optional.of(app));

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> getLogUseCase.getLogs("appId", "another@example.com"));
        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void getLogs_withUsername_correctOwner_shouldReturnLogs() {
        LogEntry log = new LogEntry().setMessage("test");
        when(applicationRepository.findById("appId")).thenReturn(Optional.of(app));
        when(logRepository.findByAppKeyAndAccountKeyOrderByTimestampDesc(any(), any()))
                .thenReturn(List.of(log));

        List<LogEntry> result = getLogUseCase.getLogs("appId", "owner@example.com");
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getMessage());
    }

    @Test
    void getFilteredLogs_shouldReturnPaginatedLogs() {
        String userEmail = "owner@example.com";

        LogEntry log1 = new LogEntry().setMessage("message1")
                .setLevel("INFO").setLoggerName("logger1").setTimestamp(LocalDateTime.now());
        LogEntry log2 = new LogEntry().setMessage("message2")
                .setLevel("ERROR").setLoggerName("logger2").setTimestamp(LocalDateTime.now());

        PaginatedResult<LogEntry> paginatedResult = new PaginatedResult<>(
                List.of(log1, log2),
                2L,
                1,
                0,
                20
        );

        final LogFilter filter = new LogFilter("INFO", "message", null, null);

        when(applicationRepository.findById(any())).thenReturn(Optional.of(app));
        when(logRepository.findFiltered(app.getKey(), app.getAccountKey(), filter, 0, 20))
                .thenReturn(paginatedResult);
        when(userAccountService.getAllAccountKeys(userEmail)).thenReturn(Set.of(""));

        PaginatedResult<LogEntry> result = getLogUseCase.getFilteredLogs(app.getId(), userEmail, filter, 0, 20);

        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getTotalPages());
    }
}
