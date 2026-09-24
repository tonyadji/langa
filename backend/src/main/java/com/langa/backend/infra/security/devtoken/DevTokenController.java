package com.langa.backend.infra.security.devtoken;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development endpoint returning a real access token for a user, without the frontend:
 * <ol>
 *     <li>{@code POST /api/dev/token/start {username, signUp?, displayName?}}: a one-time code is sent to the
 *     user's email; with {@code signUp: true}, an unknown user is created (an existing one just signs in)</li>
 *     <li>{@code POST /api/dev/token/complete {continuationToken, code}}: returns the access token</li>
 * </ol>
 * Only exists when {@code application.security.dev-token.enabled=true}; never enable it in production.
 */
@RestController
@RequestMapping(DevTokenController.PATH)
@ConditionalOnProperty(prefix = "application.security.dev-token", name = "enabled", havingValue = "true")
public class DevTokenController {

    public static final String PATH = "/api/dev/token";

    private final DevTokenProvider devTokenProvider;

    public DevTokenController(DevTokenProvider devTokenProvider) {
        this.devTokenProvider = devTokenProvider;
    }

    @PostMapping("/start")
    public ResponseEntity<StartResponse> start(@Valid @RequestBody StartRequest request) {
        DevTokenProvider.DevTokenChallenge challenge = devTokenProvider.start(
                request.username().trim(), Boolean.TRUE.equals(request.signUp()), request.displayName());
        return ResponseEntity.ok(new StartResponse(
                challenge.continuationToken(), challenge.sentTo(), challenge.codeLength(), challenge.signUp()));
    }

    @PostMapping("/complete")
    public ResponseEntity<TokenResponse> complete(@Valid @RequestBody CompleteRequest request) {
        DevTokenProvider.DevToken token = devTokenProvider.complete(request.continuationToken(), request.code().trim());
        return ResponseEntity.ok(new TokenResponse(token.accessToken(), token.tokenType(), token.expiresIn()));
    }

    /**
     * @param signUp      create the user when it does not exist (default false)
     * @param displayName display name of the created user (default: the part of the email before '@')
     */
    public record StartRequest(@NotBlank String username, Boolean signUp, String displayName) {
    }

    /** @param signUp whether the user is being created */
    public record StartResponse(String continuationToken, String codeSentTo, Integer codeLength, boolean signUp) {
    }

    public record CompleteRequest(@NotBlank String continuationToken, @NotBlank String code) {
    }

    public record TokenResponse(String accessToken, String tokenType, Long expiresIn) {
    }
}
