package com.langa.backend.infra.persistence.repositories.users.mongo;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.UserRepository;
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
    private UserRepository userRepository;

    public User toUser() {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setAccountKey(accountKey);
        return user;
    }

    public static UserDocument of(User user) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(user.getId());
        userDocument.setEmail(user.getEmail());
        userDocument.setPassword(user.getPassword());
        userDocument.setAccountKey(user.getAccountKey());
        return userDocument;
    }
}
