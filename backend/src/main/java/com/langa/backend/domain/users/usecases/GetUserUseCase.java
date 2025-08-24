package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfo me(String username) {
        return userRepository.findByEmail(username)
                .map(this::toUserInfo)
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getEmail(), user.getAccountKey());
    }
}
