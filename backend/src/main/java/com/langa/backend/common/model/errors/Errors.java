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
    TEAM_NOT_FOUND(400, "400-201", "Team not found"),
    TEAM_INVITATION_EXISTING(400, "400-202", "Team invitation existing"),
    TEAM_MEMBER_ALREADY(400, "400-203" , "The user you tried to invite is already a team member"),
    TEAM_INVITATION_NOTFOUND_OR_EXPIRED(400, "400-204" , "Team invitation not found or expired"),
    TEAM_INVITATION_INVALID_STATUS(400, "400-205" , "Team invitation invalid status"),

    INVALID_CREDENTIALS(401,"401-000" ,"Invalid credentials"),

    ACCESS_DENIED(403,"403-000", "Access denied"),

    VALIDATION_ERROR(400,"400", "Validation error"),

    INTERNAL_SERVER_ERROR(500,"500", "Internal server error"),

    NOTIFICATION_MAIL_ERROR(500,"500-001" , "Error sending notification email" );

    private final int httpCode;
    private final String code;
    private final String message;

    Errors(int httpCode, String code, String message) {
        this.httpCode = httpCode;
        this.code = code;
        this.message = message;
    }
}
