package com.langa.backend.infra.rest.teams.dto;

public record AcceptInvitationRequest(String teamId, String invitationToken) {
}
