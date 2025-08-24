package com.langa.backend.infra.persistence.repositories.applications.mongo;

import com.langa.backend.domain.applications.Application;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "applications")
public class ApplicationDocument {
    private String id;
    private String name;
    private String key;
    private String accountKey;
    private String owner;

    public Application toApplication() {
        Application application = new Application();
        application.setId(id);
        application.setName(name);
        application.setKey(key);
        application.setAccountKey(accountKey);
        application.setOwner(owner);
        return application;
    }

    public static ApplicationDocument of(Application application) {
        ApplicationDocument applicationDocument = new ApplicationDocument();
        applicationDocument.setId(application.getId());
        applicationDocument.setName(application.getName());
        applicationDocument.setKey(application.getKey());
        applicationDocument.setAccountKey(application.getAccountKey());
        applicationDocument.setOwner(application.getOwner());
        return applicationDocument;
    }
}
