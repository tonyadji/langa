package com.langa.backend.domain.applications.usecases;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetApplicationsUseCaseTest {

    protected static final String OWNER = "LangaOwner";
    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    UserAccountService userAccountService;

    @InjectMocks
    private GetApplicationsUseCase useCase;


    @Test
    void getApplications_shouldReturnEmptyList() {
        when(applicationRepository.findByOwner(OWNER)).thenReturn(Collections.emptyList());
        when(userAccountService.getAccountKey(OWNER)).thenReturn("accountKey");

        List<ApplicationInfo> result = useCase.getApplications(OWNER);

        assertTrue(result.isEmpty());
        verify(applicationRepository, times(1)).findByOwner(OWNER);
    }

    @Test
    void getApplicationsByOwner_shouldReturnApps() {
        Application app1 = Application.createNew("Langa1", "key1", OWNER);
        Application app2 = Application.createNew("Lang2","key2", OWNER);

        when(applicationRepository.findByOwner(OWNER)).thenReturn(List.of(app1, app2));
        when(userAccountService.getAccountKey(OWNER)).thenReturn("accountKey");

        List<ApplicationInfo> result = useCase.getApplications(OWNER);

        assertEquals(2, result.size());
        assertEquals("Langa1", result.get(0).name());
        verify(applicationRepository, times(1)).findByOwner(OWNER);
    }
}