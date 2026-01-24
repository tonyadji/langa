package com.langa.backend.domain.teams.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class TeamException extends GenericException {
    public TeamException(String message, Throwable cause, Errors error) {
        super(message, cause, error);
    }
}
