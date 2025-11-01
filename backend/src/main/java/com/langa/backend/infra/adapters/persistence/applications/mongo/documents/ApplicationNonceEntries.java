package com.langa.backend.infra.adapters.persistence.applications.mongo.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "c_application_nonce")
public class ApplicationNonceEntries {

    @Id
    private String id;
    private String appKey;
    private String nonce;
    private LocalDateTime usageDate;

    public ApplicationNonceEntries(String appKey, String nonce, LocalDateTime usageDate) {
        this.appKey = appKey;
        this.nonce = nonce;
        this.usageDate = usageDate;
    }
}
