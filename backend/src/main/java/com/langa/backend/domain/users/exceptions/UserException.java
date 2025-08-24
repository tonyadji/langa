package com.langa.backend.domain.users.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class UserException extends GenericException {

    public UserException(String message, Throwable cause, Errors errorCode) {
        super(message, cause, errorCode);
    }
}
