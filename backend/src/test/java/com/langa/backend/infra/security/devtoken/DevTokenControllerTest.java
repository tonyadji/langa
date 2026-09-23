package com.langa.backend.infra.security.devtoken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevTokenControllerTest {

    @Mock
    private DevTokenProvider devTokenProvider;

    @InjectMocks
    private DevTokenController controller;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(DevTokenProperties.class, DevTokenConfiguration.class, DevTokenController.class);

    @Test
    void start_shouldSendTheCodeAndReturnTheContinuationToken() {
        when(devTokenProvider.startSignIn("user@example.com"))
                .thenReturn(new DevTokenProvider.DevTokenChallenge("ct-2", "u***@example.com", 8));

        ResponseEntity<DevTokenController.StartResponse> response =
                controller.start(new DevTokenController.StartRequest(" user@example.com "));

        assertEquals(new DevTokenController.StartResponse("ct-2", "u***@example.com", 8), response.getBody());
    }

    @Test
    void complete_shouldReturnTheAccessToken() {
        when(devTokenProvider.completeSignIn("ct-2", "12345678"))
                .thenReturn(new DevTokenProvider.DevToken("eyJ.access", "Bearer", 3599L));

        ResponseEntity<DevTokenController.TokenResponse> response =
                controller.complete(new DevTokenController.CompleteRequest("ct-2", " 12345678 "));

        assertEquals(new DevTokenController.TokenResponse("eyJ.access", "Bearer", 3599L), response.getBody());
    }

    @Test
    void endpoint_shouldNotExistByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DevTokenController.class);
            assertThat(context).doesNotHaveBean(DevTokenProvider.class);
        });
    }

    @Test
    void endpoint_shouldExistWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "application.security.dev-token.enabled=true",
                        "application.security.dev-token.client-id=test-client-id",
                        "application.security.dev-token.native-auth-uri=https://langa-test.ciamlogin.com/t/oauth2/v2.0",
                        "application.security.dev-token.scope=api://api-client-id/access_as_user")
                .run(context -> {
                    assertThat(context).hasSingleBean(DevTokenController.class);
                    assertThat(context).hasSingleBean(EntraNativeAuthDevTokenProvider.class);
                });
    }

    @Test
    void endpoint_shouldFailFastWhenEnabledWithoutConfiguration() {
        contextRunner
                .withPropertyValues("application.security.dev-token.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }
}
