package com.langa.backend.infra.adapters.in.rest.ingest;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.adapters.in.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.application.services.applications.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {
    private static final String X_USER_AGENT = "X-USER-AGENT";
    private static final String X_AGENT_SIGNATURE = "X-AGENT-SIGNATURE";
    private static final String X_APP_KEY = "X-APP-KEY";
    private static final String X_ACCOUNT_KEY = "X-ACCOUNT-KEY";
    private static final String X_TIMESTAMP = "X-TIMESTAMP";
    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<Void> receiveLogs(
            @RequestHeader(X_USER_AGENT) String userAgent,
            @RequestHeader(X_AGENT_SIGNATURE) String agentSignature,
            @RequestHeader(X_APP_KEY) String appKey,
            @RequestHeader(X_ACCOUNT_KEY) String accountKey,
            @RequestHeader(X_TIMESTAMP) String timestamp,
            @RequestBody @Valid IngestionRequestDto ingestionRequestDto) {
        final IngestionCredentials credentials = new IngestionCredentials(
                userAgent,
                appKey,
                accountKey,
                timestamp,
                agentSignature,
                IngestionSecurity.CredentialType.HTTP
        );
        ingestionService.process(ingestionRequestDto, credentials);
        return ResponseEntity.accepted().build();
    }
}
