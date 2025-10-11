package com.langa.backend.domainexchange.teams;

import com.langa.backend.common.model.ShareWithInfo;

public interface TeamService {
    String getTeamKey(String teamKey);
    ShareWithInfo getShareWithInfo(String teamKey);
}
