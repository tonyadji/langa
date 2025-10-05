package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class FirstConnectionMailTemplate extends EmailTemplate {

    private static final String NAME_KEY = "username";
    private static final String TOKEN_KEY = "firstConnectionToken";
    private static final String RECIPIENTS_KEY = "recipients";
    private final String baseUrl;

    public FirstConnectionMailTemplate(@Value("${application.base-url}") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSubject() {
        return "First Connection";
    }

    @Override
    public String getMessage() {
        return "Hello " +variables.get(NAME_KEY).toString() + " ! Your account has been successfully created." +
                "\nPlease follow the link "+baseUrl+
                "/api/first-connection?token="+variables.get(TOKEN_KEY).toString()+" to complete setup and collaborating";
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.FIRST_CONNECTION_MAIL.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof FirstConnectionMailEvent firstConnectionMailEvent) {
            variables.put(RECIPIENTS_KEY, List.of(firstConnectionMailEvent.email()));
            variables.put(NAME_KEY, firstConnectionMailEvent.email());
            variables.put(TOKEN_KEY, firstConnectionMailEvent.firstConnectionToken());
        }
    }
}
