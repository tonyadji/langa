package com.langa.backend.domain.teams.services;

import com.langa.backend.domain.teams.Team;

public interface TeamMemberShipService {
    Team addMemberToTeam(String teamKey, String memberEmail);
}
