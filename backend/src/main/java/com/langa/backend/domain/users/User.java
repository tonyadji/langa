package com.langa.backend.domain.users;

import com.langa.backend.common.model.AbstractModel;
import com.langa.backend.common.utils.KeyGenerator;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class User extends AbstractModel {
    private String id;
    private String email;
    private String password;
    private String accountKey;

    public User() {}

    public static User createNew(String email, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.accountKey = KeyGenerator.generateAccountKey(email);
        return user;
    }
}
