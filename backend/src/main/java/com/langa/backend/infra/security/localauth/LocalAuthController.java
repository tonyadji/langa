package com.langa.backend.infra.security.localauth;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * Local sign-in: {@code POST /api/auth/local/token {email}} returns an access token for that email, without
 * password. The user is provisioned on its first API call, as with any identity provider.
 * Only exists when {@code application.security.local-auth.enabled=true}; never enable it in production.
 */
@RestController
@RequestMapping(LocalAuthController.PATH)
@ConditionalOnProperty(prefix = "application.security.local-auth", name = "enabled", havingValue = "true")
public class LocalAuthController {

    public static final String PATH = "/api/auth/local";

    /** Checked here too: bean validation is not active at runtime when no provider is on the classpath. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+$");

    private final LocalTokenIssuer tokenIssuer;

    public LocalAuthController(LocalTokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
        if (request.email() == null || !EMAIL.matcher(request.email().trim()).matches()) {
            throw new GenericException("A valid email is required", null, Errors.VALIDATION_ERROR);
        }
        LocalTokenIssuer.IssuedToken token = tokenIssuer.issue(request.email());
        return ResponseEntity.ok(new TokenResponse(token.accessToken(), token.tokenType(), token.expiresIn()));
    }

    public record TokenRequest(@NotBlank @Email String email) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }
}
