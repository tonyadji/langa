package com.langa.backend.infra.rest.ingest;

import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.services.applications.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<Void> receiveLogs(@RequestBody @Valid IngestionRequestDto ingestionRequestDto) {
        ingestionService.process(ingestionRequestDto);
        return ResponseEntity.accepted().build();
    }
}
