package com.langa.backend.infra.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Demo data created at startup so that a local installation is not empty: a demo user owning a demo application
 * with a few hours of logs and metrics. The application key and secret are fixed, so that a sample application
 * can be configured in advance (see the root docker-compose.yml). Disabled by default.
 */
@Component
@ConfigurationProperties(prefix = "application.demo")
@Getter
@Setter
public class DemoDataProperties {

    private boolean enabled = false;
    /** Owner of the demo application. */
    private String email = "demo@langa.local";
    private String appId = "00000000-0000-0000-0000-00000000d3e0";
    private String appName = "Demo Shop";
    /** Ingestion key of the demo application (must not contain "-lga-"). */
    private String appKey = "APP-DemoShop";
    /** Ingestion secret of the demo application: public, for local use only. */
    private String appSecret = "langa-demo-secret-for-local-use-only";
    /** How far back the generated history goes. */
    private int historyHours = 24;
}
