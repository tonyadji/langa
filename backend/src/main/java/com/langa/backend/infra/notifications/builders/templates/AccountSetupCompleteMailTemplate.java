package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AccountSetupCompleteMailTemplate extends EmailTemplate {

    private static final String NAME_KEY = "username";
    private static final String RECIPIENTS_KEY = "recipients";
    private final String baseUrl;

    public AccountSetupCompleteMailTemplate(@Value("${application.base-url}") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSubject() {
        return "Account Setup Complete";
    }

    @Override
    public String getMessage() {
        return "Hello " +variables.get(NAME_KEY).toString() + " ! Your account setup is now complete and you can proceed with login" +
                "\nPlease follow the link "+baseUrl+ "/login" ;
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.ACCOUNT_SETUP_COMPLETE_MAIL.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof AccountSetupCompleteMailEvent accountSetupCompleteMailEvent) {
            variables.put(RECIPIENTS_KEY, List.of(accountSetupCompleteMailEvent.email()));
            variables.put(NAME_KEY, accountSetupCompleteMailEvent.email());
        }
    }
}
