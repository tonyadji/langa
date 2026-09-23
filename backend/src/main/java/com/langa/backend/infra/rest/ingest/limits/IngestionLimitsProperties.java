package com.langa.backend.infra.rest.ingest.limits;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Protection of the public HTTP ingestion endpoint, checked before the payload is parsed.
 */
@Component
@ConfigurationProperties(prefix = "application.ingestion.limits")
@Getter
@Setter
public class IngestionLimitsProperties {

    /** Maximum size of an ingestion request body, in bytes (default 5 MB). */
    private long maxPayloadBytes = 5L * 1024 * 1024;
    /** Maximum ingestion requests per minute and per application key; 0 disables the limit. */
    private int requestsPerMinute = 1200;
}
