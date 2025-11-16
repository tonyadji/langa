package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ApplicationSharedTemplate extends EmailTemplate {

    private static final String NAME_KEY = "appName";
    private static final String RECIPIENTS_KEY = "recipients";
    private final String baseUrl;

    public ApplicationSharedTemplate(@Value("${application.base-url}") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSubject() {
        return "Application sharing";
    }

    @Override
    public String getMessage() {
        return "Good news ! \nApplication " +variables.get(NAME_KEY).toString() + " has been shared with you or your team." +
                "\nLogin to your account to check it out : "+baseUrl+ "/login" ;
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.APPLICATION_SHARED_EVENT.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof ApplicationSharedEvent applicationSharedEvent) {
            variables.put(RECIPIENTS_KEY, List.of(applicationSharedEvent.sharedWithEmail()));
            variables.put(NAME_KEY, applicationSharedEvent.appName());
        }
    }
}
