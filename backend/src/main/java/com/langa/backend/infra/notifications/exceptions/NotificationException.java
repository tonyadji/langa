package com.langa.backend.infra.notifications.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class NotificationException extends GenericException {
    public NotificationException(String message, Throwable cause, Errors error) {
        super(message, cause, error);
    }
}
