package com.langa.agent.core.helpers;

public record IngestionParamsResolver(String ingestionUrl, String secret) {

    public String resolveHttpUrl() {
        return ingestionUrl;
    }

    public String resolveSenderType() {
        return ingestionUrl;
    }

    public String resolveBootStrapServer() {
        return ingestionUrl;
    }

    public String resolveTopic() {
        return ingestionUrl;
    }

    public String resolveAppKey() {
        return ingestionUrl;
    }

    public String resolveAccountKey() {
        return ingestionUrl;
    }

    public String resolveSecret() {
        return secret;
    }
}
