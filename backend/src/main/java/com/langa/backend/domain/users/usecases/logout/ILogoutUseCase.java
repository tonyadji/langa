package com.langa.backend.domain.users.usecases.logout;

import com.langa.backend.domain.users.valueobjects.AuthTokens;

public interface ILogoutUseCase {

    AuthTokens execute(LogoutCommand command);
}
