package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.usecases.CreateApplicationUseCase;
import com.langa.backend.domain.applications.usecases.GetApplicationsUseCase;
import com.langa.backend.domain.applications.usecases.GetLogUseCase;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.CreateApplicationRequestDto;
import com.langa.backend.infra.rest.common.dto.LogDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private GetApplicationsUseCase getApplicationsUseCase;

    @Mock
    private GetLogUseCase getLogUseCase;

    @Mock
    private CreateApplicationUseCase createApplicationUseCase;

    @InjectMocks
    private ApplicationController applicationController;

    @Test
    void createApplication_shouldReturnApplicationDto() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        CreateApplicationRequestDto requestDto = new CreateApplicationRequestDto("MyApp");

        ApplicationInfo appInfo = new ApplicationInfo("appId", "MyApp", "appKey123", "accountKey123", "user@example.com");
        when(createApplicationUseCase.create("MyApp", "user@example.com")).thenReturn(appInfo);

        ResponseEntity<ApplicationDto> response = applicationController.createApplication(userDetails, requestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("MyApp", response.getBody().name());
        verify(createApplicationUseCase, times(1)).create("MyApp", "user@example.com");
    }

    @Test
    void getAllApplications_shouldReturnListOfApplicationDto() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        ApplicationInfo app1 = new ApplicationInfo("1", "App1", "key1", "account1", "user@example.com");
        ApplicationInfo app2 = new ApplicationInfo("2", "App2", "key2", "account1", "user@example.com");

        when(getApplicationsUseCase.getApplications("user@example.com")).thenReturn(List.of(app1, app2));

        ResponseEntity<List<ApplicationDto>> response = applicationController.getAllApplications(userDetails);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(getApplicationsUseCase, times(1)).getApplications("user@example.com");
    }

    @Test
    void getLogsByAppKey_shouldReturnListOfLogDto() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        LogEntry log1 = new LogEntry()
                .setAppKey("appKey1")
                .setAccountKey("accountKey1")
                .setLevel("INFO")
                .setMessage("message1")
                .setTimestamp(LocalDateTime.now());
        LogEntry log2 = new LogEntry()
                .setAppKey("appKey1")
                .setAccountKey("accountKey1")
                .setLevel("ERROR")
                .setMessage("message2")
                .setTimestamp(LocalDateTime.now());

        when(getLogUseCase.getLogs("app1", "user@example.com")).thenReturn(List.of(log1, log2));

        ResponseEntity<List<LogDto>> response = applicationController.getLogsByAppKey(userDetails, "app1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(getLogUseCase, times(1)).getLogs("app1", "user@example.com");
    }
}