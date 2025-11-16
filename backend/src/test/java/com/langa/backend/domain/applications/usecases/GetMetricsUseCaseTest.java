package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.repositories.MetricEntryRepository;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;
import com.langa.backend.domainexchange.teams.TeamService;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMetricsUseCaseTest {

    @InjectMocks
    private GetMetricsUseCase getMetricsUseCase;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private MetricEntryRepository metricEntryRepository;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private TeamService teamService;


    @Test
    void testGetFilteredMetrics_ValidApplicationAndFilter() {
        String appId = "app123";
        String userEmail = "user@example.com";
        int page = 0;
        int size = 10;

        Application mockApp = Application.createNew("app-key123", "U-accountkey123", userEmail);

        MetricFilter filter = new MetricFilter();
        MetricEntry metric = new MetricEntry();
        PaginatedResult<MetricEntry> mockPageResult = new PaginatedResult<>(
                Collections.singletonList(metric), 1, 1, page, size);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(mockApp));
        when(metricEntryRepository.findFiltered(mockApp.getKey(), mockApp.getAccountKey(), filter, page, size))
                .thenReturn(mockPageResult);
        when(userAccountService.getAllAccountKeys(userEmail)).thenReturn(Collections.singleton("U-accountkey123"));

        PaginatedResult<MetricEntry> result = getMetricsUseCase.getFilteredMetrics(appId, userEmail, filter, page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(metric, result.getContent().get(0));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(page, result.getPage());

        verify(applicationRepository, times(1)).findById(appId);
        verify(metricEntryRepository, times(1)).findFiltered(mockApp.getKey(), mockApp.getAccountKey(), filter, page, size);
    }


    @Test
    void testGetFilteredMetrics_ApplicationNotFound() {
        String appId = "invalidApp";
        String userEmail = "user@example.com";
        MetricFilter filter = new MetricFilter();

        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> getMetricsUseCase.getFilteredMetrics(appId, userEmail, filter, 0, 10));

        assertEquals("Application not found with id: " + appId, exception.getMessage());
        assertEquals(Errors.APPLICATION_NOT_FOUND, exception.getError());

        verify(applicationRepository, times(1)).findById(appId);
        verifyNoInteractions(metricEntryRepository);
    }


    @Test
    void testGetFilteredMetrics_UserNotOwner() {
        String appId = "app123";
        String userEmail = "unauthorizedUser@example.com";

        Application mockApp = Application.createNew("app-key123", "U-accountkey123", "owner@example.com");

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(mockApp));


        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> getMetricsUseCase.getFilteredMetrics(appId, userEmail, new MetricFilter(), 0, 10));

        assertEquals("Application access", exception.getMessage());
        assertEquals(Errors.ACCESS_DENIED, exception.getError());

        verify(applicationRepository, times(1)).findById(appId);
        verifyNoInteractions(metricEntryRepository);
    }

    @Test
    void testGetFilteredMetrics_NoMetricsFound() {
        String appId = "app123";
        String userEmail = "user@example.com";
        int page = 0;
        int size = 10;

        Application mockApp = Application.createNew("app-key123", "U-accountkey123", userEmail);

        MetricFilter filter = new MetricFilter();
        PaginatedResult<MetricEntry> mockPageResult = new PaginatedResult<>(
                Collections.emptyList(), 0, 0, page, size);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(mockApp));
        when(metricEntryRepository.findFiltered(mockApp.getKey(), mockApp.getAccountKey(), filter, page, size))
                .thenReturn(mockPageResult);
        when(userAccountService.getAllAccountKeys(userEmail)).thenReturn(Collections.singleton("U-accountkey123"));


        PaginatedResult<MetricEntry> result = getMetricsUseCase.getFilteredMetrics(appId, userEmail, filter, page, size);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(page, result.getPage());

        verify(applicationRepository, times(1)).findById(appId);
        verify(metricEntryRepository, times(1)).findFiltered(mockApp.getKey(), mockApp.getAccountKey(), filter, page, size);
    }
}