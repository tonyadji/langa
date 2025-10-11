package com.langa.backend.infra.outbox.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class OutboxException extends GenericException {
    public OutboxException(String message, Throwable cause, Errors error) {
        super(message, cause, error);
    }
}
