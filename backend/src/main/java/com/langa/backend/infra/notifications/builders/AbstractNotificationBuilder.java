package com.langa.backend.infra.notifications.builders;

import com.langa.backend.infra.notifications.mail.EmailNotification;
import com.langa.backend.infra.notifications.model.Notification;

import java.util.List;

public abstract class AbstractNotificationBuilder {

    protected AbstractNotificationBuilder() {}

    protected static class NotificationBuilder {
        private String subject;
        private String body;
        private List<String> recipients;

        public static NotificationBuilder getBuilder() {
            return new NotificationBuilder();
        }

        public Notification buildEmailNotification() {
            return new EmailNotification()
                    .setRecipients(recipients)
                    .setSubject(subject)
                    .setBody(body);
        }

        public NotificationBuilder withRecipients(List<String> recipients) {
            this.recipients = recipients;
            return this;
        }

        public NotificationBuilder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public NotificationBuilder withBody(String body) {
            this.body = body;
            return this;
        }


    }
}
