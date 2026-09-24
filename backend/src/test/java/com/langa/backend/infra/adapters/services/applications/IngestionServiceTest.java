package com.langa.backend.infra.adapters.services.applications;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.domain.applications.usecases.ingest.IngestionUseCase;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private IngestionUseCase ingestionUseCase;

    @InjectMocks
    private IngestionService ingestionService;

    @Test
    void process_shouldDelegateToUseCase() {
        LogIngestionRequestDto dto = new LogIngestionRequestDto("app-key", "acc-key", List.of(), IngestionType.LOG);
        IngestionCredentials credentials = new IngestionCredentials("agent", "app-key", "acc-key", "123", "sig",
                IngestionSecurity.CredentialType.HTTP);

        ingestionService.process(dto, credentials);

        verify(ingestionUseCase).process(dto, credentials);
    }
}
