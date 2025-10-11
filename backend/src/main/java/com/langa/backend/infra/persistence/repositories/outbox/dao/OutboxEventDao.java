package com.langa.backend.infra.persistence.repositories.outbox.dao;

import com.langa.backend.infra.persistence.repositories.outbox.document.OutboxEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxEventDao extends MongoRepository<OutboxEventDocument, String> {
    List<OutboxEventDocument> findByProcessed(boolean processed);
}
