package com.langa.backend.domain.applications.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class ApplicationException extends GenericException {

    public ApplicationException(String message, Throwable cause, Errors errorCode) {
        super(message, cause, errorCode);
    }
}
