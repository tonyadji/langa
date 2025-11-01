package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.valueobjects.ShareWith;
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

    public Application toApplication() {
        return Application.populate(id, name, key, accountKey, owner, sharedWith);
    }

    public Application toSecuredApplication() {
        return Application.populateSecured(id, name, key, accountKey, secret, ingestionUri, owner, sharedWith);
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
        return applicationDocument;
    }
}
