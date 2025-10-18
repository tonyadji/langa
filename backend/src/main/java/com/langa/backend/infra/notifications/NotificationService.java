package com.langa.backend.infra.notifications;

import com.langa.backend.infra.notifications.model.Notification;

public interface NotificationService {
    void send(Notification notification);
}
