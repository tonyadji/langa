package com.langa.backend.infra.adapters.in.rest.advice;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ApiError> apiError(GenericException gex) {
        log.error("Business exception: {}", gex.getMessage(), gex);
        return ResponseEntity.status(gex.getError().getHttpCode())
                .body(ApiError.of(gex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", details);

        return ResponseEntity.badRequest()
                .body(ApiError.of(
                        Errors.VALIDATION_ERROR.getMessage(),
                        Errors.VALIDATION_ERROR.getCode(),
                        details
                ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500)
                .body(ApiError.of(
                        Errors.INTERNAL_SERVER_ERROR.getMessage(),
                        Errors.INTERNAL_SERVER_ERROR.getCode(), ex.getMessage()));
    }
}
