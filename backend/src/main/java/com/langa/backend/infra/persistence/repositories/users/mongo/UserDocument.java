package com.langa.backend.infra.persistence.repositories.users.mongo;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "c_users")
public class UserDocument {
    @Id
    private String id;
    private String email;
    private String password;
    private String accountKey;
    private UserStatus userStatus;
    private String firstConnectionToken;

    public User toUser() {
        return User.populate(id, email, password, accountKey, userStatus, firstConnectionToken);
    }

    public static UserDocument of(User user) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(user.getId());
        userDocument.setEmail(user.getEmail());
        userDocument.setPassword(user.getPassword());
        userDocument.setAccountKey(user.getAccountKey());
        userDocument.setUserStatus(user.getStatus());
        userDocument.setFirstConnectionToken(user.getFirstConnectionToken());
        return userDocument;
    }
}
