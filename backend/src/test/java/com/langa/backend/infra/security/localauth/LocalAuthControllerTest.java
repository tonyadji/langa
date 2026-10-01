package com.langa.backend.infra.security.localauth;

import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.infra.security.config.AuthProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalAuthControllerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(AuthProviderProperties.class, LocalAuthProperties.class,
                    LocalAuthConfiguration.class, LocalAuthController.class)
            .withPropertyValues(
                    "application.security.auth.issuer-uri=langa-local",
                    "application.security.auth.audiences=langa-local");

    @Test
    void token_shouldReturnAnAccessTokenForTheEmail() {
        LocalTokenIssuer issuer = new LocalTokenIssuer(
                LocalTokenIssuerTest.localProperties(), Duration.ofHours(2), LocalTokenIssuer.generateKeyPair());
        LocalAuthController controller = new LocalAuthController(issuer);

        ResponseEntity<LocalAuthController.TokenResponse> response =
                controller.token(new LocalAuthController.TokenRequest("demo@langa.local"));

        LocalAuthController.TokenResponse body = response.getBody();
        assertEquals("Bearer", body.tokenType());
        assertEquals(7200, body.expiresIn());
        assertEquals("demo@langa.local", NimbusJwtDecoder.withPublicKey(issuer.publicKey()).build()
                .decode(body.accessToken()).getClaimAsString("email"));
    }

    @Test
    void token_shouldRejectInvalidEmails() {
        LocalAuthController controller = new LocalAuthController(new LocalTokenIssuer(
                LocalTokenIssuerTest.localProperties(), Duration.ofHours(2), LocalTokenIssuer.generateKeyPair()));

        for (String email : new String[]{null, " ", "not-an-email", "a b@c.d"}) {
            LocalAuthController.TokenRequest request = new LocalAuthController.TokenRequest(email);
            assertThrows(GenericException.class, () -> controller.token(request), String.valueOf(email));
        }
    }

    @Test
    void endpoint_shouldNotExistByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(LocalAuthController.class);
            assertThat(context).doesNotHaveBean(LocalTokenIssuer.class);
        });
    }

    @Test
    void endpoint_shouldExistWhenEnabledWithTheLocalProvider() {
        contextRunner
                .withPropertyValues(
                        "application.security.local-auth.enabled=true",
                        "application.security.local-auth.token-ttl=30m",
                        "application.security.auth.provider=local")
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalAuthController.class);
                    assertThat(context.getBean(LocalTokenIssuer.class).issue("a@b.c").expiresIn()).isEqualTo(1800);
                });
    }

    @Test
    void startup_shouldFail_whenEnabledWithAnotherProvider() {
        contextRunner
                .withPropertyValues(
                        "application.security.local-auth.enabled=true",
                        "application.security.auth.provider=entra")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("application.security.auth.provider=local"));
    }
}
