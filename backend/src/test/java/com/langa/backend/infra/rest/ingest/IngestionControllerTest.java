package com.langa.backend.infra.rest.ingest;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.adapters.services.applications.IngestionService;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IngestionControllerTest {

    @Mock
    private IngestionService ingestionService;

    @Test
    void receiveLogs_shouldForwardCredentialsAndPayload_andReturnAccepted() {
        IngestionController controller = new IngestionController(ingestionService);
        LogIngestionRequestDto dto = new LogIngestionRequestDto("app-key", "acc-key", List.of(), IngestionType.LOG);

        ResponseEntity<Void> response = controller.receiveLogs("agent-v1", "nonce:sig", "app-key", "acc-key", "12345", dto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        ArgumentCaptor<IngestionCredentials> captor = ArgumentCaptor.forClass(IngestionCredentials.class);
        verify(ingestionService).process(org.mockito.ArgumentMatchers.eq(dto), captor.capture());
        assertEquals("app-key", captor.getValue().appKey());
        assertEquals("acc-key", captor.getValue().accountKey());
        assertEquals("agent-v1", captor.getValue().userAgent());
        assertEquals("12345", captor.getValue().timestamp());
        assertEquals("nonce:sig", captor.getValue().signature());
    }
}
