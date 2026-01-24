package com.langa.backend.domain.teams.valueobjects;

import java.time.LocalDateTime;

public record TeamInvitationPeriod(LocalDateTime inviteDate, LocalDateTime expiryDate) {
}
