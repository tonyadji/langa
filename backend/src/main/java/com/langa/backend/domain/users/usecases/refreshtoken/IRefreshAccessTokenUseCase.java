package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.domain.users.valueobjects.AuthTokens;

public interface IRefreshAccessTokenUseCase {

    AuthTokens execute(RefreshAccessTokenCommand command);
}
