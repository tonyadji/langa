package com.langa.backend.domain.applications;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.events.ApplicationCreatedEvent;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Getter
public class Application extends AbstractModel {

    private final ApplicationId appId;
    private final String name;
    private final String accountKey;
    private final String owner;
    private String secret;
    private String ingestionUri;
    private Set<ShareWith> sharedWith;
    private ApplicationUsage usage;


    private long pendingLogBytes = 0;
    private long pendingMetricBytes = 0;
    private List<LogEntry> newLogEntries = new ArrayList<>();
    private List<MetricEntry> newMetricsEntries = new ArrayList<>();


    private Application(String name, String accountKey, String owner) {
        this.appId = ApplicationId.newId();
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.secret = KeyGenerator.generateAppSecret();
        this.ingestionUri = KeyGenerator.generateIngestionUri(accountKey, appId.key());
        sharedWith = new HashSet<>();
    }

    private Application(ApplicationId appId, String name, String accountKey, String owner, Set<ShareWith> sharedWith, ApplicationUsage usage) {
        this.appId = appId;
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.sharedWith = sharedWith == null ? new HashSet<>() : sharedWith;
        this.usage = usage == null ? ApplicationUsage.empty() : usage;
    }

    private Application(ApplicationId appId, String name, String accountKey, String secret, String ingestionUri, String owner, Set<ShareWith> sharedWith, ApplicationUsage usage) {
        this.appId = appId;
        this.name = name;
        this.accountKey = accountKey;
        this.secret = secret;
        this.ingestionUri = ingestionUri;
        this.owner = owner;
        this.sharedWith = sharedWith == null ? new HashSet<>() : sharedWith;
        this.usage = usage == null ? ApplicationUsage.empty() : usage;
    }

    public static Application populate(ApplicationId appId, String name, String accountKey, String owner, Set<ShareWith> sharedWith, ApplicationUsage usage) {
        return new Application(appId, name, accountKey, owner, sharedWith, usage);
    }

    public static Application createNew(String name, String accountKey, String owner) {
        final Application application = new Application(name, accountKey, owner);
        application.registerDomainEvent(ApplicationCreatedEvent.of(application));
        return application;
    }

    public static Application populateSecured(ApplicationId appId, String name, String accountKey, String secret, String ingestionUri, String owner, Set<ShareWith> sharedWith, ApplicationUsage usage) {
        return new Application(appId, name, accountKey, secret, ingestionUri, owner, sharedWith, usage);
    }

    public void createLogEntries(List<LogEntry> logs, IngestionSizeCalculator ingestionSizeCalculator) {
        newLogEntries = logs.stream()
                .map(entry -> entry
                        .setAppKey(appId.key())
                        .setAccountKey(accountKey))
                .toList();
        pendingLogBytes = ingestionSizeCalculator.calculateSizeInBytes(newLogEntries);
        this.usage = this.usage.increaseLogBytes(pendingLogBytes);
    }

    public void createMetricEntries(List<MetricEntry> metrics, IngestionSizeCalculator ingestionSizeCalculator) {
        newMetricsEntries = metrics.stream()
                .map(entry -> entry
                        .setAppKey(appId.key())
                        .setAccountKey(accountKey))
                .toList();
        pendingMetricBytes = ingestionSizeCalculator.calculateSizeInBytes(newMetricsEntries);
        this.usage = this.usage.increaseTotalMetricBytes(pendingMetricBytes);
    }

    public void checkOwnership(String username) {
        if (!Objects.equals(owner, username)) {
            throw new ApplicationException("Application ownership", null, Errors.ACCESS_DENIED);
        }
    }

    public void authorizedToAccess(String username, Set<String> accountKeys) {
        if (Objects.equals(owner, username)) {
            return;
        }
        boolean hasActiveShare = accountKeys.stream().anyMatch(this::alreadySharedWith);
        if (!hasActiveShare) {
            throw new ApplicationException("Application access", null, Errors.ACCESS_DENIED);
        }
    }

    public boolean alreadySharedWith(String accountOrTeamKey) {
        return sharedWith.stream().anyMatch(shareWith -> isSharedWith(accountOrTeamKey).test(shareWith));
    }

    public ShareWith shareWith(String accountOrTeamKey, SharedWithProfile profile) {
        final ShareWith shareWith = new ShareWith(appId.id(), name, accountOrTeamKey, profile, LocalDateTime.now(), null, null);
        this.sharedWith.add(shareWith);
        return shareWith;
    }

    public void revokeSharing(String accountOrTeamKey) {
        final ShareWith activeSharing = sharedWith.stream()
                .filter(shareWith -> Objects.equals(shareWith.key(), accountOrTeamKey) && shareWith.isCurrentlyActive())
                .findFirst()
                .orElseThrow(() -> new ApplicationException("No sharing found to revoke", null, Errors.APPLICATION_SHARING_NOT_FOUND_TO_REVOKE));
        this.sharedWith.remove(activeSharing);
        final ShareWith revokedSharing = new ShareWith(activeSharing.appId(), activeSharing.appName(), activeSharing.key(),
                activeSharing.profile(), activeSharing.sharedDate(), activeSharing.expirationDate(), LocalDateTime.now());
        this.sharedWith.add(revokedSharing);
    }


    private Predicate<ShareWith> isSharedWith(String accountOrTeamKey) {
        return sw -> Objects.equals(sw.key(), accountOrTeamKey)
                && sw.isCurrentlyActive();
    }

    public String getId() {
        return appId.id();
    }

    public String getKey() {
        return appId.key();
    }

    public boolean isOwnedOrSharedWith(String username, String accountKey) {
        return Objects.equals(owner, username) ||
                sharedWith.stream().anyMatch(shareWith -> Objects.equals(shareWith.key(), accountKey));
    }
}
