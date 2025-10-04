package com.langa.backend.domain.teams;

import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class TeamInvitation {
    private String id;
    private String team;
    private String host;
    private String guest;
    private String invitationToken;
    private LocalDateTime inviteDate;
    private LocalDateTime expiryDate;
    private InvitationStatus status;
}
