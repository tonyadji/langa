package com.langa.backend.domain.users;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Getter;

import java.util.Objects;


@Getter
public class User extends AbstractModel {
    private final UserId userId;

    private String password;
    private UserStatus status;
    private String firstConnectionToken;


    private User(UserId userId, String password, UserStatus status, String firstConnectionToken) {
        this.userId = userId;
        this.password = password;
        this.status = status;
        this.firstConnectionToken = firstConnectionToken;
    }

    public static User populate(UserId id, String password, UserStatus status, String firstConnectionToken) {
        return new User(id, password, status, firstConnectionToken);
    }

    public static User createActive(String email, String encodedPassword) {
        String accountKey = KeyGenerator.generateAccountKey(email);
        final UserId id = UserId.newId().withEmail(email).withAccountKey(accountKey);
        final User activeUser = new User(id, encodedPassword, UserStatus.ACTIVE, null);
        activeUser.registerDomainEvent(ActiveUserRegisteredEvent.of(activeUser));
        return activeUser;
    }

    public static User createNew(String email, String encodedPassword) {
        String accountKey = KeyGenerator.generateAccountKey(email);
        final UserId id = UserId.newId().withEmail(email).withAccountKey(accountKey);
        return new User(id, encodedPassword, UserStatus.CREATED, null);
    }

    public void buildFirstConnectionToken() {
        firstConnectionToken = KeyGenerator.genericToken(userId.accountKey(), userId.email());
    }


    public void completeFirstConnection(String encodedPassword) {
        if(UserStatus.ACTIVE.equals(status)) {
            throw new UserException("User is already active", null, Errors.USER_ILLEGAL_STATUS);
        }
        if(!Objects.isNull(encodedPassword)) {
            this.password = encodedPassword;
        }
        this.status = UserStatus.ACTIVE;
        this.registerDomainEvent(AccountSetupCompleteMailEvent.of(this));
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
