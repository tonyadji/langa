package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class InvitationAcceptedMailTemplate extends EmailTemplate {

    private static final String NAME_KEY = "username";
    private static final String RECIPIENTS_KEY = "recipients";

    public InvitationAcceptedMailTemplate() {
        super();
    }

    @Override
    public String getSubject() {
        return "First Connection";
    }

    @Override
    public String getMessage() {
        return "Welcome " +variables.get(NAME_KEY).toString() + " !" +
                "\nYou will shortly receive and email to help you complete the setup of your account.";
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.INVITATION_ACCEPTED_EMAIL.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof InvitationAcceptedMailEvent invitationAcceptedMailEvent) {
            variables.put(RECIPIENTS_KEY, List.of(invitationAcceptedMailEvent.member()));
            variables.put(NAME_KEY, invitationAcceptedMailEvent.member());
        }
    }
}
