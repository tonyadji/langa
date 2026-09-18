package com.langa.backend.infra.security.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class SecurityException extends GenericException {
    public SecurityException(String message, Throwable cause, Errors error) {
        super(message, cause, error);
    }
}
