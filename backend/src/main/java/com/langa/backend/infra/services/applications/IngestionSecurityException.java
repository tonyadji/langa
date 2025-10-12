package com.langa.backend.infra.services.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class IngestionSecurityException extends GenericException {
    public IngestionSecurityException(String message, Throwable cause, Errors error) {
        super(message, cause, error);
    }
}
