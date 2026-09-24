package com.langa.backend.infra.notifications.mail;

import com.langa.backend.infra.notifications.model.Notification;
import com.langa.backend.infra.notifications.model.NotificationType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EmailNotification implements Notification {

    private List<String> recipients;
    private String subject;
    private String body;

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
