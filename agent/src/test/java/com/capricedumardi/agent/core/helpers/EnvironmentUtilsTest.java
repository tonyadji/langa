package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.config.ConfigLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentUtilsTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("langa.ingestion.url");
        System.clearProperty("langa.ingestion.secret");
        ConfigLoader.reloadConfig();
    }

    @Test
    void getIngestionParamsResolverReflectsCurrentConfig() {
        String creds = Base64.getEncoder().encodeToString("acc-lga-app".getBytes());
        System.setProperty("langa.ingestion.url", "https://host.example/api/ingestion/http/" + creds);
        System.setProperty("langa.ingestion.secret", "s3cr3t");
        ConfigLoader.reloadConfig();

        IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();

        assertEquals("app", resolver.resolveAppKey());
        assertEquals("s3cr3t", resolver.resolveSecret());
    }
}
