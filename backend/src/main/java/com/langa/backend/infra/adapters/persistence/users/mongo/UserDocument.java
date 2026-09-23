package com.langa.backend.infra.adapters.persistence.users.mongo;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "c_users")
public class UserDocument {
    @Id
    private String id;
    private String email;
    @Indexed(unique = true, sparse = true)
    private String externalId;
    private String accountKey;
    private UserStatus userStatus;

    public User toUser() {
        return User.populate(UserId.of(id, email, accountKey), externalId, userStatus);
    }

    public static UserDocument of(User user) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(user.getId());
        userDocument.setEmail(user.getEmail());
        userDocument.setExternalId(user.getExternalId());
        userDocument.setAccountKey(user.getAccountKey());
        userDocument.setUserStatus(user.getStatus());
        return userDocument;
    }
}
