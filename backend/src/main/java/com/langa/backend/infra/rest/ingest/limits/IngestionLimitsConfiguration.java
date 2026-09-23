package com.langa.backend.infra.rest.ingest.limits;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Clock;

@Configuration
public class IngestionLimitsConfiguration {

    static final String INGESTION_PATH = "/api/ingestion";

    @Bean
    public FilterRegistrationBean<IngestionLimitsFilter> ingestionLimitsFilter(IngestionLimitsProperties properties) {
        IngestionRateLimiter rateLimiter = new IngestionRateLimiter(properties.getRequestsPerMinute(), Clock.systemUTC());
        FilterRegistrationBean<IngestionLimitsFilter> registration =
                new FilterRegistrationBean<>(new IngestionLimitsFilter(properties, rateLimiter));
        registration.addUrlPatterns(INGESTION_PATH, INGESTION_PATH + "/*");
        // Before the security filter chain: rejected requests are not parsed nor authenticated
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
