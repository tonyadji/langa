package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.domain.users.valueobjects.AuthTokens;

public interface ILoginUseCase {

    AuthTokens execute(LoginCommand command);
}
