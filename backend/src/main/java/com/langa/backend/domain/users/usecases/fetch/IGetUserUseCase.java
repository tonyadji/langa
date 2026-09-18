package com.langa.backend.domain.users.usecases.fetch;

import com.langa.backend.domain.users.valueobjects.UserInfo;

public interface IGetUserUseCase {

    UserInfo queryByUsername(String username);
    UserInfo queryByFirstConnectionToken(String username);
}
