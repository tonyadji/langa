package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.usecases.IngestLogUseCase;
import com.langa.backend.infra.rest.applications.dto.LogRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Deprecated
public class LogEntryController {

    private final IngestLogUseCase ingestLogUseCase;

    public ResponseEntity<Void> receiveLogs(@RequestBody @Valid LogRequestDto logRequestDto) {
        ingestLogUseCase.process(logRequestDto);
        return ResponseEntity.accepted().build();
    }
}
