package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.CreateApplicationRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateApplicationControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    @Test
    void createApplication_shouldReturnCreatedWithDto() {
        CreateApplicationController controller = new CreateApplicationController(commandBusDispatcher);
        UserDetails userDetails = new User("owner@example.com", "pw", List.of());
        ApplicationInfo info = new ApplicationInfo("id-1", "My App", "key-1", "acc-1", "owner@example.com", Collections.emptySet());
        when(commandBusDispatcher.dispatch(any())).thenReturn(info);

        ResponseEntity<ApplicationDto> response = controller.createApplication(userDetails, new CreateApplicationRequestDto("My App"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("My App", response.getBody().name());
    }
}
