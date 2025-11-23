package com.langa.backend.domain.users.usecases.getinfo;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.UserInfo;

@UseCase
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfo me(GetUserInfoQuery query) {
        query.validate();

        return userRepository.findByEmail(query.username())
                .map(this::toUserInfo)
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));
    }

    public UserInfo findByFirstConnectionToken(String token) {
        return userRepository.findByFistConnectionToken(token)
                .map(this::toUserInfo)
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getUserId().email(), user.getAccountKey().value());
    }
}
