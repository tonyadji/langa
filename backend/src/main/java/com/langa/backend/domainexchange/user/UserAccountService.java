package com.langa.backend.domainexchange.user;

import com.langa.backend.common.model.ShareWithInfo;

import java.util.Set;

public interface UserAccountService {
    String getAccountKey(String userEmail);
    Set<String> getTeamKeys(String username);
    Set<String> getAllAccountKeys(String username);
    ShareWithInfo getShareWithInfo(String userEmail);
}
