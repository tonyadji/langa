package com.langa.backend.infra.adapters.in.rest.applications.dto;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.valueobjects.ShareWith;
import lombok.Getter;

import java.util.Set;

@Getter
public final class SecuredApplicationDto {

    private final String id;
    private final String name;
    private final String key;
    private final String accountKey;
    private final String owner;
    private final String secret;
    private final String ingestionUri;
    private final Set<ShareWith> sharedWith;
    private String http;
    private String kafka;

    private SecuredApplicationDto(String id, String name, String key, String accountKey, String secret, String ingestionUri, String owner, Set<ShareWith> sharedWith) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.accountKey = accountKey;
        this.secret = secret;
        this.ingestionUri = ingestionUri;
        this.owner = owner;
        this.sharedWith = sharedWith;
    }

    public static SecuredApplicationDto of(Application application, String httpPrefix, String kafkaPrefix) {
        return new SecuredApplicationDto(
                application.getId(),
                application.getName(),
                application.getKey(),
                application.getAccountKey(),
                application.getSecret(),
                application.getIngestionUri(),
                application.getOwner(),
                application.getSharedWith()
        ).setHttpAndKafkaUrl(httpPrefix, kafkaPrefix);
    }

    private SecuredApplicationDto setHttpAndKafkaUrl(String httpPrefix, String kafkaPrefix) {
        this.http = httpPrefix + ingestionUri;
        this.kafka = kafkaPrefix + ingestionUri;
        return this;
    }
}
