package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.infra.config.LangaApplicationProperties;
import com.langa.backend.infra.rest.applications.dto.SecuredApplicationDto;
import com.langa.backend.infra.rest.applications.dto.UpdateApplicationRetentionPolicyDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateApplicationPolicyControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    @Test
    void createApplication_shouldDispatchUpdateCommand_andReturnSecuredDto() {
        LangaApplicationProperties properties = new LangaApplicationProperties();
        properties.setEndpoint("/api/ingestion");
        properties.setKafkaUrl("localhost:9092");
        properties.setKafkaTopic("langa");
        UpdateApplicationPolicyController controller = new UpdateApplicationPolicyController(properties, commandBusDispatcher);
        UserDetails userDetails = new User("owner@example.com", "pw", List.of());
        Application app = Application.createNew("My App", "acc-1", "owner@example.com");
        when(commandBusDispatcher.dispatch(any())).thenReturn(app);

        ResponseEntity<SecuredApplicationDto> response = controller.createApplication(
                userDetails, "app-1", new UpdateApplicationRetentionPolicyDto(10, ChronoUnit.DAYS));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(app.getSecret(), response.getBody().getSecret());
    }
}
