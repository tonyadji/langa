package com.langa.backend.infra.kafka.exceptions;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;

public class KafkaIngestionException extends GenericException {
    public KafkaIngestionException(String message, Throwable cause, Errors errors) {
        super(message,  cause, errors);
    }
}
