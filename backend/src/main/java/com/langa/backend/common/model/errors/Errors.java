package com.langa.backend.common.model.errors;

import lombok.Getter;

@Getter
public enum Errors {

    APPLICATION_NOT_FOUND(400,"400-000", "Application not found"),
    APPLICATION_NAME_ALREADY_EXISTS(400,"400-001", "Application name already exists"),
    APPLICATION_ALREADY_SHARED(400, "400-002","Application already shared"),
    APPLICATION_AUTO_SHARE_FORBIDDEN(400, "400-003","Application auto share forbidden"),
    APPLICATION_SHARING_NOT_FOUND_TO_REVOKE(400, "400-004","Application sharing not found to revoke"),

    USER_NOT_FOUND(400,"400-101", "User not found"),
    USERNAME_ALREADY_EXISTS(400,"400-102", "Username already exists"),
    PASSWORDS_MISMATCH(400,"400-103", "Passwords and confirmation do not match"),
    USER_ILLEGAL_STATUS(400,"400-104" , "User status is illegal to perform this action" ),

    TEAM_NAME_ALREADY_EXISTS(400, "400-200", "Team name already exists"),
    TEAM_NOT_FOUND(400, "400-201", "Team not found"),
    TEAM_INVITATION_EXISTING(400, "400-202", "Team invitation existing"),
    TEAM_MEMBER_ALREADY(400, "400-203" , "The user you tried to invite is already a team member"),
    TEAM_INVITATION_NOTFOUND_OR_EXPIRED(400, "400-204" , "Team invitation not found or expired"),
    TEAM_INVITATION_INVALID_STATUS(400, "400-205" , "Team invitation invalid status"),

    ILLEGAL_INGESTION_REQUEST(400,"400-401" ,"Illegal ingestion request"),

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
