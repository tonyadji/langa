package com.langa.backend.infra.adapters.services.applications;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.usecases.IngestionUseCase;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionService {

    private final IngestionUseCase ingestionUseCase;

    public IngestionService(IngestionUseCase ingestionUseCase) {
        this.ingestionUseCase = ingestionUseCase;
    }

    @Transactional
    public void process(IngestionRequestDto ingestionRequestDto, IngestionCredentials ingestionCredentials) {
        ingestionUseCase.process(ingestionRequestDto, ingestionCredentials);
    }
}
