package com.langa.backend.domain.users;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;


@Data
@Accessors(chain = true)
public class User extends AbstractModel {
    private String id;
    private String email;
    private String password;
    private String accountKey;
    private UserStatus status;
    private String firstConnectionToken;

    public static User createActive(String email, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.accountKey = KeyGenerator.generateAccountKey(email);
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public static User createNew(String email, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.accountKey = KeyGenerator.generateAccountKey(email);
        user.status = UserStatus.CREATED;
        return user;
    }

    public void buildFirstConnectionToken() {
        firstConnectionToken = KeyGenerator.genericToken(accountKey, email);
    }


    public void couldCouldCompleteFirstConnection(String encodedPassword) {
        if(UserStatus.ACTIVE.equals(status)) {
            throw new UserException("User is already active", null, Errors.USER_ILLEGAL_STATUS);
        }
        if(!Objects.isNull(encodedPassword)) {
            this.password = encodedPassword;
        }
        this.status = UserStatus.ACTIVE;
    }
}
