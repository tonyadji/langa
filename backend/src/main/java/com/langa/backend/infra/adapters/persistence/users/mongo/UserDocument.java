package com.langa.backend.infra.adapters.persistence.users.mongo;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "c_users")
@CompoundIndex(name = "external_identity", def = "{'identityProvider': 1, 'externalId': 1}", unique = true, sparse = true)
public class UserDocument {

    /** Provider of the users linked before the identity provider was stored alongside the external id. */
    static final String LEGACY_IDENTITY_PROVIDER = "entra";

    @Id
    private String id;
    private String email;
    private String identityProvider;
    private String externalId;
    private String accountKey;
    private UserStatus userStatus;

    public User toUser() {
        final String provider = identityProvider == null && externalId != null ? LEGACY_IDENTITY_PROVIDER : identityProvider;
        return User.populate(UserId.of(id, email, accountKey), provider, externalId, userStatus);
    }

    public static UserDocument of(User user) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(user.getId());
        userDocument.setEmail(user.getEmail());
        userDocument.setIdentityProvider(user.getIdentityProvider());
        userDocument.setExternalId(user.getExternalId());
        userDocument.setAccountKey(user.getAccountKey());
        userDocument.setUserStatus(user.getStatus());
        return userDocument;
    }
}
