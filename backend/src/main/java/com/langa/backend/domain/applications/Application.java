package com.langa.backend.domain.applications;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.infra.rest.common.dto.LogDto;
import com.langa.backend.infra.rest.common.dto.MetricDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class Application extends AbstractModel {

    private String id;
    private String name;
    private String key;
    private String accountKey;
    private String owner;

    public Application() {}

    private Application(String name, String accountKey, String owner) {
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.key = KeyGenerator.generateAppKey();
    }

    public static Application createNew(String name, String accountKey, String owner) {
        return new Application(name, accountKey, owner);
    }

    public List<LogEntry> createLogEntries(List<LogDto> logs) {
        return logs.stream()
                .map(logDto -> logDto.toLogEntry()
                        .setAppKey(key)
                        .setAccountKey(accountKey))
                .toList();
    }

    public List<MetricEntry> createMetricEntries(List<MetricDto> metrics) {
        return metrics.stream()
                .map(metricDto -> metricDto.toMetricEntry()
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
