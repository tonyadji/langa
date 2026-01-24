package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.valueobjects.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@Document(collection = "c_applications")
public class ApplicationDocument {
    @Id
    private String id;
    private String name;
    private String key;
    private String accountKey;
    private String secret;
    private String ingestionUri;
    private String owner;
    private Set<ShareWith> sharedWith;
    private ApplicationUsage usage;
    private RetentionPolicy retentionPolicy;

    public Application toApplication() {
        return Application.populate(ApplicationId.of(id, key), name, owner(accountKey, owner), sharedWith, usage);
    }

    public Application toSecuredApplication() {
        return Application.populateSecured(ApplicationId.of(id, key), name, owner(accountKey, owner), secret, ingestionUri, sharedWith, usage, retentionPolicy);
    }

    public static ApplicationDocument of(Application application) {
        ApplicationDocument applicationDocument = new ApplicationDocument();
        applicationDocument.setId(application.getId());
        applicationDocument.setName(application.getName());
        applicationDocument.setKey(application.getKey());
        applicationDocument.setAccountKey(application.getAccountKey());
        applicationDocument.setOwner(application.getOwner());
        applicationDocument.setSharedWith(application.getSharedWith());
        applicationDocument.setSecret(application.getSecret());
        applicationDocument.setIngestionUri(application.getIngestionUri());
        applicationDocument.setUsage(application.getUsage());
        applicationDocument.setRetentionPolicy(application.getRetentionPolicy());
        return applicationDocument;
    }

    private ApplicationOwner owner(String accountKey, String owner) {
        return new ApplicationOwner(accountKey, owner);
    }
}
