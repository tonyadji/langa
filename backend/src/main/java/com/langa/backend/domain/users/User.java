package com.langa.backend.domain.users;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Getter;

import java.util.Objects;


@Getter
public class User extends AbstractModel {
    private final String id;
    private final String email;
    private String password;
    private final String accountKey;
    private UserStatus status;
    private String firstConnectionToken;


    private User(String id, String email, String password, String accountKey, UserStatus status, String firstConnectionToken) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.accountKey = accountKey;
        this.status = status;
        this.firstConnectionToken = firstConnectionToken;
    }

    public static User populate(String id, String email, String password, String accountKey, UserStatus status, String firstConnectionToken) {
        return new User(id, email, password, accountKey, status, firstConnectionToken);
    }

    public static User createActive(String email, String encodedPassword) {
        String accountKey = KeyGenerator.generateAccountKey(email);
        return new User(null, email, encodedPassword, accountKey, UserStatus.ACTIVE, null);
    }

    public static User createNew(String email, String encodedPassword) {
        String accountKey = KeyGenerator.generateAccountKey(email);
        return new User(null, email, encodedPassword, accountKey, UserStatus.CREATED, null);
    }

    public void buildFirstConnectionToken() {
        firstConnectionToken = KeyGenerator.genericToken(accountKey, email);
    }


    public AccountSetupCompleteMailEvent completeFirstConnection(String encodedPassword) {
        if(UserStatus.ACTIVE.equals(status)) {
            throw new UserException("User is already active", null, Errors.USER_ILLEGAL_STATUS);
        }
        if(!Objects.isNull(encodedPassword)) {
            this.password = encodedPassword;
        }
        this.status = UserStatus.ACTIVE;
        return AccountSetupCompleteMailEvent.of(this);
    }
}
