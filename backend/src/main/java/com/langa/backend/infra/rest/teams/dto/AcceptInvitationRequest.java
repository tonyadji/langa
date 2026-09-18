package com.langa.backend.infra.rest.teams.dto;

public record AcceptInvitationRequest(String guest, String invitationToken) {
}
