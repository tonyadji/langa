package com.langa.backend.infra.rest.ingest.limits;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class IngestionPayloadTooLargeException extends GenericException {
    public IngestionPayloadTooLargeException(long maxPayloadBytes) {
        super("Ingestion payload exceeds " + maxPayloadBytes + " bytes", null, Errors.INGESTION_PAYLOAD_TOO_LARGE);
    }
}
