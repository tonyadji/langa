package com.langa.backend.infra.persistence.repositories.users;

import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.repositories.TeamMemberRepository;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domainexchange.user.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    public static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public UserAccountServiceImpl(UserRepository userRepository, TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public String getAccountKey(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UserException(
                        USER_NOT_FOUND,
                        null,
                        Errors.USER_NOT_FOUND
                ))
                .getAccountKey();
    }

    @Override
    public Set<String> getTeamKeys(String username) {
        return teamMemberRepository.findTeamsKeysByMemberUsername(username);
    }

    @Override
    public Set<String> getAllAccountKeys(String username) {
        String userKey = userRepository.findByEmail(username).orElseThrow(() -> new UserException(
                        USER_NOT_FOUND,
                        null,
                        Errors.USER_NOT_FOUND
                ))
                .getAccountKey();
        Set<String> accessKeys = new HashSet<>(Set.of(userKey));
        Set<String> teamKeys = teamMemberRepository.findTeamsKeysByMemberUsername(username);
        accessKeys.addAll(teamKeys);
        log.info("Access keys for user {}: {}", username, accessKeys);
        return accessKeys;
    }

    @Override
    public ShareWithInfo getShareWithInfo(String userEmail) {
        return userRepository.findByEmailOrAccountKey(userEmail)
                .map(user -> new ShareWithInfo(user.getAccountKey(), user.getEmail()))
                .orElseThrow(() -> new UserException(
                        USER_NOT_FOUND,
                        null,
                        Errors.USER_NOT_FOUND
                ));
    }
}
