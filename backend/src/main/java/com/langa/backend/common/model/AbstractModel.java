package com.langa.backend.common.model;

import com.langa.backend.common.eda.model.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModel {
    private List<DomainEvent> events;

    protected AbstractModel() {
        events = new ArrayList<>();
    }

    public List<DomainEvent> getEvents() {
        return events;
    }
    public  void clearEvents() {
        events.clear();
    }
}
