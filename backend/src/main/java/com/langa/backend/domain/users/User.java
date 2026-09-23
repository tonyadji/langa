package com.langa.backend.domain.users;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Getter;

import java.util.Objects;


@Getter
public class User extends AbstractModel {
    private final UserId userId;

    /** Identity provider the user signs in with (e.g. entra, cognito). */
    private String identityProvider;
    /** Identifier of the user in that identity provider. */
    private String externalId;
    private UserStatus status;


    private User(UserId userId, String identityProvider, String externalId, UserStatus status) {
        this.userId = userId;
        this.identityProvider = identityProvider;
        this.externalId = externalId;
        this.status = status;
    }

    public static User populate(UserId id, String identityProvider, String externalId, UserStatus status) {
        return new User(id, identityProvider, externalId, status);
    }

    /**
     * Creates a user on its first sign-in through the identity provider.
     */
    public static User createFromExternalIdentity(ExternalIdentity identity) {
        final User activeUser = new User(newUserId(identity.email().toLowerCase()),
                identity.provider(), identity.subject(), UserStatus.ACTIVE);
        activeUser.registerDomainEvent(ActiveUserRegisteredEvent.of(activeUser));
        return activeUser;
    }

    /**
     * Creates a user that has been invited (e.g. in a team) but has not signed in yet.
     */
    public static User createInvited(String email) {
        return new User(newUserId(email), null, null, UserStatus.CREATED);
    }

    private static UserId newUserId(String email) {
        String accountKey = KeyGenerator.generateAccountKey(email);
        return UserId.newId().withEmail(email).withAccountKey(accountKey);
    }

    /**
     * Links an existing local user (created before the identity provider migration, or invited)
     * to its identity provider account.
     */
    public void linkExternalIdentity(ExternalIdentity identity) {
        if (Objects.isNull(identity)) {
            throw new UserException("External identity is required", null, Errors.USER_ILLEGAL_STATUS);
        }
        if (isLinked() && !isLinkedTo(identity)) {
            throw new UserException("User is already linked to another identity", null, Errors.USER_ILLEGAL_STATUS);
        }
        this.identityProvider = identity.provider();
        this.externalId = identity.subject();
        this.status = UserStatus.ACTIVE;
    }

    public boolean isLinked() {
        return externalId != null;
    }

    private boolean isLinkedTo(ExternalIdentity identity) {
        return Objects.equals(identityProvider, identity.provider()) && Objects.equals(externalId, identity.subject());
    }

    public String getEmail() {
        return userId.email();
    }

    public String getAccountKey() {
        return userId.accountKey();
    }

    public String getId() {
        return userId.id();
    }
}
