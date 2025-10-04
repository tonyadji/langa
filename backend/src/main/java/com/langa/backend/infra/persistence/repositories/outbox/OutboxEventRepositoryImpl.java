package com.langa.backend.infra.persistence.repositories.outbox;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.common.eda.repositories.OutboxEventRepository;
import com.langa.backend.infra.persistence.repositories.outbox.dao.OutboxEventDao;
import com.langa.backend.infra.persistence.repositories.outbox.document.OutboxEventDocument;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventDao outboxEventDao;

    public OutboxEventRepositoryImpl(OutboxEventDao outboxEventDao) {
        this.outboxEventDao = outboxEventDao;
    }

    @Override
    public void save(OutboxEvent event) {
        outboxEventDao.save(OutboxEventDocument.of(event));
    }

    @Override
    public List<OutboxEvent> findAllByProcessedFalse() {
        return outboxEventDao.findByProcessed(false)
                .stream()
                .map(OutboxEventDocument::toOutboxEvent)
                .toList();
    }
}
