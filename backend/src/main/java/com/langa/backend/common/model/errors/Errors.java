package com.langa.backend.common.model.errors;

import lombok.Getter;

@Getter
public enum Errors {

    APPLICATION_NOT_FOUND(400,"400-000", "Application not found"),
    APPLICATION_NAME_ALREADY_EXISTS(400,"400-001", "Application name already exists"),

    USER_NOT_FOUND(400,"400-101", "User not found"),
    USERNAME_ALREADY_EXISTS(400,"400-102", "Username already exists"),
    PASSWORDS_MISMATCH(400,"400-103", "Passwords do not match"),

    TEAM_NAME_ALREADY_EXISTS(400, "400-200", "Team name already exists"),

    INVALID_CREDENTIALS(401,"401-000" ,"Invalid credentials"),

    ACCESS_DENIED(403,"403-000", "Access denied"),

    VALIDATION_ERROR(400,"400", "Validation error"),

    INTERNAL_SERVER_ERROR(500,"500", "Internal server error");

    private int httpCode;
    private String code;
    private String message;

    Errors(int httpCode, String code, String message) {
        this.httpCode = httpCode;
        this.code = code;
        this.message = message;
    }
}
