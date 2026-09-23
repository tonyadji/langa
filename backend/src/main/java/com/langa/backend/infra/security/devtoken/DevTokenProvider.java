package com.langa.backend.infra.security.devtoken;

/**
 * Obtains a real access token for a user without the frontend, in two steps:
 * the identity provider sends a one-time code by email, then the code is exchanged for the token.
 * For development and tests only (see {@link DevTokenProperties}).
 */
public interface DevTokenProvider {

    /**
     * Sends a one-time code to the user's email.
     *
     * @param username    email of the user
     * @param signUp      when the user does not exist, create it instead of failing;
     *                    an existing user always follows the normal sign-in
     * @param displayName display name of the created user (default: the part of the email before '@')
     */
    DevTokenChallenge start(String username, boolean signUp, String displayName);

    /** Exchanges the code received by email for an access token (completing the sign-up if needed). */
    DevToken complete(String continuationToken, String code);

    /**
     * @param continuationToken opaque token to send back with the code
     * @param sentTo            masked address the code was sent to
     * @param codeLength        expected length of the code, if known
     * @param signUp            whether the user is being created
     */
    record DevTokenChallenge(String continuationToken, String sentTo, Integer codeLength, boolean signUp) {
    }

    record DevToken(String accessToken, String tokenType, Long expiresIn) {
    }
}
