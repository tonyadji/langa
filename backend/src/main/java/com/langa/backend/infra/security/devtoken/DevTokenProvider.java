package com.langa.backend.infra.security.devtoken;

/**
 * Obtains a real access token for a user without the frontend, in two steps:
 * the identity provider sends a one-time code by email, then the code is exchanged for the token.
 * For development and tests only (see {@link DevTokenProperties}).
 */
public interface DevTokenProvider {

    /** Sends a one-time code to the user's email. */
    DevTokenChallenge startSignIn(String username);

    /** Exchanges the code received by email for an access token. */
    DevToken completeSignIn(String continuationToken, String code);

    /**
     * @param continuationToken opaque token to send back with the code
     * @param sentTo            masked address the code was sent to
     * @param codeLength        expected length of the code, if known
     */
    record DevTokenChallenge(String continuationToken, String sentTo, Integer codeLength) {
    }

    record DevToken(String accessToken, String tokenType, Long expiresIn) {
    }
}
