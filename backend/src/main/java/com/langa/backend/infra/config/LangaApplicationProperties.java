package com.langa.backend.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.ingestion")
@Setter
public class LangaApplicationProperties {
    @Value("${application.base-url}")
    private String baseUrl;
    @Getter
    @Value("${application.front-url}")
    private String frontUrl;
    private String endpoint;
    private String kafkaUrl;
    private String kafkaTopic;

    public String getHttpPrefix() {
        return baseUrl + endpoint + "/h/";
    }

    public String getKafkaPrefix() {
        return kafkaUrl + endpoint + "/" + kafkaTopic + "/";
    }
}
