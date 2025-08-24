package com.langa.backend.domainexchange.user;

import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.common.model.errors.Errors;
import org.springframework.stereotype.Component;

@Component
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepository;

    public UserAccountServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getAccountKey(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserException(
                        "User not found",
                        null,
                        Errors.USER_NOT_FOUND
                ))
                .getAccountKey();
    }
}
