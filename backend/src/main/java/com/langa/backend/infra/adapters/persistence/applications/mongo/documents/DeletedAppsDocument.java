package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "c_deleted_applications")
public class DeletedAppsDocument {
    @Id
    private String id;
    private ApplicationDocument snapshot;
    private Instant deletedDate;
    private String reason;

    public static DeletedAppsDocument of(ApplicationDocument application) {
        final DeletedAppsDocument deletedAppsDocument = new DeletedAppsDocument();
        application.setSharedWith(null);
        deletedAppsDocument.setSnapshot(application);
        deletedAppsDocument.setDeletedDate(Instant.now());
        deletedAppsDocument.setReason("User request");
        return deletedAppsDocument;
    }
}
