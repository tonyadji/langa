package com.langa.backend.domain.applications.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.usecases.fetch.GetApplicationsUseCase;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import com.langa.backend.domainexchange.user.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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
        assertEquals("Langa1", result.getFirst().name());
        verify(applicationRepository, times(1)).findByOwner(OWNER);
    }

    @Test
    void getApplicationsByOwner_shouldIncludeSharedApps() {
        Application shared = Application.createNew("Shared App", "other-key", "other-owner");
        when(applicationRepository.findByOwner(OWNER)).thenReturn(Collections.emptyList());
        when(userAccountService.getAccountKey(OWNER)).thenReturn("accountKey");
        when(userAccountService.getTeamKeys(OWNER)).thenReturn(Set.of("team-1"));
        when(applicationRepository.findBySharedWithUser("accountKey")).thenReturn(List.of(shared));
        when(applicationRepository.findBySharedWithTeams(Set.of("team-1"))).thenReturn(Collections.emptyList());

        List<ApplicationInfo> result = useCase.getApplications(OWNER);

        assertEquals(1, result.size());
        assertNull(result.get(0).key());
    }

    @Test
    void getApplications_noArg_shouldReturnAllOwnedApplications() {
        Application app = Application.createNew("Langa1", "key1", OWNER);
        when(applicationRepository.findAll()).thenReturn(List.of(app));

        List<ApplicationInfo> result = useCase.getApplications();

        assertEquals(1, result.size());
    }

    @Test
    void getApplication_shouldReturnApplication_whenOwnedOrShared() {
        Application app = Application.createNew("Langa1", "key1", OWNER);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getAccountKey(OWNER)).thenReturn("key1");

        Application result = useCase.getApplication("app-1", OWNER);

        assertEquals(app, result);
    }

    @Test
    void getApplication_shouldThrow_whenNotFound() {
        when(applicationRepository.findById("app-1")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> useCase.getApplication("app-1", OWNER));
    }

    @Test
    void getApplication_shouldThrow_whenNoAccess() {
        Application app = Application.createNew("Langa1", "key1", OWNER);
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userAccountService.getAccountKey("intruder")).thenReturn("some-other-key");

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> useCase.getApplication("app-1", "intruder"));
        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void getSecuredApplication_shouldReturnApplication_whenFound() {
        Application app = Application.createNew("Langa1", "key1", OWNER);
        when(applicationRepository.securedFindByIdAndOwner("app-1", OWNER)).thenReturn(Optional.of(app));

        assertEquals(app, useCase.getSecuredApplication("app-1", OWNER));
    }

    @Test
    void getSecuredApplication_shouldThrow_whenNotFound() {
        when(applicationRepository.securedFindByIdAndOwner("app-1", OWNER)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> useCase.getSecuredApplication("app-1", OWNER));
    }
}