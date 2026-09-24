package com.langa.backend.infra.adapters.persistence.applications.mongo.daos;

import com.langa.backend.infra.adapters.persistence.applications.mongo.documents.DeletedAppsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDeletedApplicationDao extends MongoRepository<DeletedAppsDocument, String> {

}
