package com.langa.agent.core.helpers;

public class EnvironmentUtils {

    public static IngestionParamsResolver getIngestionParamsResolver() {
        String ingestionUrl = getEnvOrProperty("LANGA_URL", "langa.url", null);
        String secret = getEnvOrProperty("LANGA_SECRET", "langa.secret", null);
        return new IngestionParamsResolver(ingestionUrl, secret);
    }

    /**
     * Helper method to get value from environment variable or system property
     */
    private static String getEnvOrProperty(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) {
            value = System.getProperty(propertyName, defaultValue);
        }
        return value;
    }
}
