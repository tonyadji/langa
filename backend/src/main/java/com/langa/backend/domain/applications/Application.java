package com.langa.backend.domain.applications;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.ShareWith;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Getter
public class Application extends AbstractModel {

    private final String id;
    private final String name;
    private final String key;
    private final String accountKey;
    private final String owner;
    private final Set<ShareWith> sharedWith;


    private Application(String id, String name, String accountKey, String owner) {
        this.id = id;
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.key = KeyGenerator.generateAppKey();
        sharedWith = new HashSet<>();
    }

    private Application(String id, String name, String key, String accountKey, String owner, Set<ShareWith> sharedWith) {
        this.id = id;
        this.name = name;
        this.accountKey = accountKey;
        this.owner = owner;
        this.key = key;
        this.sharedWith = sharedWith == null ? new HashSet<>() : sharedWith;
    }

    public static Application populate(String id, String name, String key, String accountKey, String owner, Set<ShareWith> sharedWith) {
        return new Application(id, name, key, accountKey, owner, sharedWith);
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
        final ShareWith shareWith = new ShareWith(id, name, accountOrTeamKey, profile, LocalDateTime.now(), null, null);
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
}
