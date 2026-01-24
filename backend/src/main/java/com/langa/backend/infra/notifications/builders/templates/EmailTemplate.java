package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public abstract class EmailTemplate {

    protected Map<String, Object> variables;
    public abstract String getSubject();
    public abstract String getMessage();
    public abstract List<String> getRecipients();

    public abstract boolean couldProcess(DomainEvent event);
    public abstract void processEvent(DomainEvent event);

    EmailTemplate() {
        this.variables = new HashMap<>();
    }
}
