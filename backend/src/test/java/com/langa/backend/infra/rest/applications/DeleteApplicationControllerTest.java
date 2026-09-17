package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteApplicationControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    @Test
    void createApplication_shouldDispatchDeleteCommand_andReturnOk() {
        DeleteApplicationController controller = new DeleteApplicationController(commandBusDispatcher);
        UserDetails userDetails = new User("owner@example.com", "pw", List.of());
        when(commandBusDispatcher.dispatch(any())).thenReturn("App Deleted Successfully");

        ResponseEntity<String> response = controller.createApplication(userDetails, "app-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("App Deleted Successfully", response.getBody());
    }
}
