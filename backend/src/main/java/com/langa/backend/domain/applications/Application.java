package com.langa.backend.domain.applications;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class Application extends AbstractModel {

    private final String id;
    private final String name;
    private final String key;
    private final String accountKey;
    private final String owner;


    private Application(String id, String name, String accountKey, String owner) {
        this.id = id;
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.key = KeyGenerator.generateAppKey();
    }

    public static Application populate(String id, String name, String accountKey, String owner) {
        return new Application(id, name, accountKey, owner);
    }

    public static Application createNew(String name, String accountKey, String owner) {
        return new Application(null, name, accountKey, owner);
    }

    public List<LogEntry> createLogEntries(List<LogEntry> logs) {
        return logs.stream()
                .map(entry -> entry
                        .setAppKey(key)
                        .setAccountKey(accountKey))
                .toList();
    }

    public List<MetricEntry> createMetricEntries(List<MetricEntry> metrics) {
        return metrics.stream()
                .map(entry -> entry
                        .setAppKey(key)
                        .setAccountKey(accountKey))
                .toList();
    }

    public void checkOwnership(String username) {
        if(!Objects.equals(owner, username)) {
            throw new ApplicationException("Application ownership", null, Errors.ACCESS_DENIED);
        }
    }
}
