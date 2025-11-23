package com.langa.backend.domain.users;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.AccountKey;
import com.langa.backend.domain.users.valueobjects.FirstConnectionToken;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Getter;

import java.util.List;
import java.util.Objects;


@Getter
public class User extends AbstractModel {
    private final UserId userId;
    private final AccountKey accountKey;
    private String password;
    private UserStatus status;
    private FirstConnectionToken firstConnectionToken;
    @Getter(lombok.AccessLevel.NONE)
    private List<DomainEvent> events;


    private User(String email, String password, AccountKey accountKey, UserStatus status, FirstConnectionToken firstConnectionToken) {
        super();
        this.userId = UserId.newId(email);
        this.password = password;
        this.accountKey = accountKey;
        this.status = status;
        this.firstConnectionToken = firstConnectionToken;
    }

    private User(UserId userId, AccountKey accountKey, UserStatus status) {
        super();
        this.userId = userId;
        this.accountKey = accountKey;
        this.status = status;
    }

    public static User populate(UserId userId, AccountKey accountKey, UserStatus status) {
        return new User(userId, accountKey, status);
    }

    public static User createActive(String email, String encodedPassword) {
        return new User(email, encodedPassword, AccountKey.newKey(email), UserStatus.ACTIVE, null);
    }

    public static User createNew(String email, String encodedPassword) {
        return new User(email, encodedPassword, AccountKey.newKey(email), UserStatus.CREATED, null);
    }

    public void buildFirstConnectionToken() {
        firstConnectionToken = FirstConnectionToken.newToken(accountKey.value(), userId.email());
    }

    public void completeRegistrationProcess(String encodedPassword) {
        if(UserStatus.ACTIVE.equals(status)) {
            throw new UserException("User is already active", null, Errors.USER_ILLEGAL_STATUS);
        }
        if(!Objects.isNull(encodedPassword)) {
            this.password = encodedPassword;
        }
        this.status = UserStatus.ACTIVE;
        registerDomainEvent(AccountSetupCompleteMailEvent.of(this));
    }

    private void registerDomainEvent(DomainEvent event) {
        events.add(event);
    }

}
