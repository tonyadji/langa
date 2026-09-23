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
 *     <li>{@code POST /api/dev/token/start {username}}: a one-time code is sent to the user's email</li>
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
        DevTokenProvider.DevTokenChallenge challenge = devTokenProvider.startSignIn(request.username().trim());
        return ResponseEntity.ok(new StartResponse(challenge.continuationToken(), challenge.sentTo(), challenge.codeLength()));
    }

    @PostMapping("/complete")
    public ResponseEntity<TokenResponse> complete(@Valid @RequestBody CompleteRequest request) {
        DevTokenProvider.DevToken token = devTokenProvider.completeSignIn(request.continuationToken(), request.code().trim());
        return ResponseEntity.ok(new TokenResponse(token.accessToken(), token.tokenType(), token.expiresIn()));
    }

    public record StartRequest(@NotBlank String username) {
    }

    public record StartResponse(String continuationToken, String codeSentTo, Integer codeLength) {
    }

    public record CompleteRequest(@NotBlank String continuationToken, @NotBlank String code) {
    }

    public record TokenResponse(String accessToken, String tokenType, Long expiresIn) {
    }
}
