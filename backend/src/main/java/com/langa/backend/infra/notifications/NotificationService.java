package com.langa.backend.infra.notifications;

import com.langa.backend.infra.notifications.model.DomainNotification;

public interface NotificationService {
    void send(DomainNotification notification);
}
