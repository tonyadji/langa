package com.langa.backend.common.model.errors;

import lombok.Getter;

public class GenericException extends RuntimeException {

    @Getter
    protected Errors error;

    public GenericException(String message) {
        super(message);
    }

    public GenericException(Errors error) {
        super(error.getMessage());
        this.error = error;
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericException(String message, Throwable cause, Errors error) {
        super(message, cause);
        this.error = error;
    }
}
