package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.ShareAppRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationSharingControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    private final UserDetails userDetails = new User("owner@example.com", "pw", List.of());

    @Test
    void shareApplication_shouldDispatchShareCommand() {
        ApplicationSharingController controller = new ApplicationSharingController(commandBusDispatcher);
        ApplicationInfo info = new ApplicationInfo("id-1", "My App", "key-1", "acc-1", "owner@example.com", Collections.emptySet());
        when(commandBusDispatcher.dispatch(any())).thenReturn(info);

        ResponseEntity<ApplicationDto> response = controller.shareApplication(
                userDetails, "app-1", new ShareAppRequestDto("guest@example.com", SharedWithProfile.USER));

        assertEquals("My App", response.getBody().name());
    }

    @Test
    void revokeApplicationSharing_shouldDispatchRevokeCommand() {
        ApplicationSharingController controller = new ApplicationSharingController(commandBusDispatcher);
        ApplicationInfo info = new ApplicationInfo("id-1", "My App", "key-1", "acc-1", "owner@example.com", Collections.emptySet());
        when(commandBusDispatcher.dispatch(any())).thenReturn(info);

        ResponseEntity<ApplicationDto> response = controller.revokeApplicationSharing(
                userDetails, "app-1", new ShareAppRequestDto("guest@example.com", SharedWithProfile.USER));

        assertEquals("My App", response.getBody().name());
    }
}
